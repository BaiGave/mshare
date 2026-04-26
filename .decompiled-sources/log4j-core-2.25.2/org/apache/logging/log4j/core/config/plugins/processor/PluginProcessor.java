/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor;

import aQute.bnd.annotation.spi.ServiceProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;
import org.apache.logging.log4j.util.Strings;

@SupportedAnnotationTypes(value={"org.apache.logging.log4j.core.config.plugins.Plugin"})
@ServiceProvider(value=Processor.class, resolution="optional")
public class PluginProcessor
extends AbstractProcessor {
    private static final Element[] EMPTY_ELEMENT_ARRAY = new Element[0];
    private static final String SUPPRESS_WARNING_PUBLIC_SETTER_STRING = "log4j.public.setter";
    public static final String PLUGIN_CACHE_FILE = "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat";
    private final List<Element> processedElements = new ArrayList<Element>();
    private final PluginCache pluginCache = new PluginCache();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = this.processingEnv.getMessager();
        if (!annotations.isEmpty()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Plugin.class);
            this.collectPlugins(elements);
            this.processedElements.addAll(elements);
            Set<? extends Element> pluginAttributeBuilderElements = roundEnv.getElementsAnnotatedWith(PluginBuilderAttribute.class);
            this.processBuilderAttribute(pluginAttributeBuilderElements);
            this.processedElements.addAll(pluginAttributeBuilderElements);
        }
        if (roundEnv.processingOver() && !this.processedElements.isEmpty()) {
            try {
                messager.printMessage(Diagnostic.Kind.NOTE, String.format("%s: writing plugin descriptor for %d Log4j Plugins to `%s`.", PluginProcessor.class.getSimpleName(), this.processedElements.size(), PLUGIN_CACHE_FILE));
                this.writeCacheFile(this.processedElements.toArray(EMPTY_ELEMENT_ARRAY));
            }
            catch (Exception e) {
                StringWriter sw = new StringWriter();
                sw.append(PluginProcessor.class.getSimpleName()).append(": unable to write plugin descriptor to file ").append(PLUGIN_CACHE_FILE).append("\n");
                e.printStackTrace(new PrintWriter(sw));
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
            }
        }
        return false;
    }

    private void processBuilderAttribute(Iterable<? extends Element> elements) {
        for (Element element : elements) {
            if (!(element instanceof VariableElement)) continue;
            this.processBuilderAttribute((VariableElement)element);
        }
    }

    private void processBuilderAttribute(VariableElement element) {
        String fieldName = element.getSimpleName().toString();
        SuppressWarnings suppress = element.getAnnotation(SuppressWarnings.class);
        if (suppress != null && Arrays.asList(suppress.value()).contains(SUPPRESS_WARNING_PUBLIC_SETTER_STRING)) {
            return;
        }
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof TypeElement) {
            TypeElement typeElement = (TypeElement)enclosingElement;
            for (Element element2 : typeElement.getEnclosedElements()) {
                ExecutableElement methodElement;
                String methodName;
                if (!(element2 instanceof ExecutableElement) || !(methodName = (methodElement = (ExecutableElement)element2).getSimpleName().toString()).toLowerCase(Locale.ROOT).startsWith("set") && !methodName.toLowerCase(Locale.ROOT).startsWith("with") || methodElement.getParameters().size() != 1) continue;
                Types typeUtils = this.processingEnv.getTypeUtils();
                boolean followsNamePattern = methodName.equals(String.format("set%s", PluginProcessor.expectedFieldNameInASetter(fieldName))) || methodName.equals(String.format("with%s", PluginProcessor.expectedFieldNameInASetter(fieldName)));
                boolean isPublicMethod = methodElement.getModifiers().contains((Object)Modifier.PUBLIC);
                boolean checkForAssignable = typeUtils.isAssignable(methodElement.getReturnType(), methodElement.getEnclosingElement().asType());
                boolean foundPublicSetter = followsNamePattern && checkForAssignable && isPublicMethod;
                if (!foundPublicSetter) continue;
                return;
            }
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("The field `%s` does not have a public setter, Note that @SuppressWarnings(\"%s\"), can be used on the field to suppress the compilation error. ", fieldName, SUPPRESS_WARNING_PUBLIC_SETTER_STRING), element);
        }
    }

    private static String expectedFieldNameInASetter(String fieldName) {
        if (fieldName.startsWith("is")) {
            fieldName = fieldName.substring(2);
        }
        return fieldName.isEmpty() ? fieldName : Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private void collectPlugins(Iterable<? extends Element> elements) {
        Elements elementUtils = this.processingEnv.getElementUtils();
        PluginElementVisitor pluginVisitor = new PluginElementVisitor(elementUtils);
        PluginAliasesElementVisitor pluginAliasesVisitor = new PluginAliasesElementVisitor(elementUtils);
        for (Element element : elements) {
            Plugin plugin = element.getAnnotation(Plugin.class);
            if (plugin == null) continue;
            PluginEntry entry = element.accept(pluginVisitor, plugin);
            Map<String, PluginEntry> category = this.pluginCache.getCategory(entry.getCategory());
            category.put(entry.getKey(), entry);
            Collection<PluginEntry> entries = element.accept(pluginAliasesVisitor, plugin);
            for (PluginEntry pluginEntry : entries) {
                category.put(pluginEntry.getKey(), pluginEntry);
            }
        }
    }

    private void writeCacheFile(Element ... elements) throws IOException {
        FileObject fileObject = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", PLUGIN_CACHE_FILE, elements);
        try (OutputStream out = fileObject.openOutputStream();){
            this.pluginCache.writeCache(out);
        }
    }

    private static final class PluginElementVisitor
    extends SimpleElementVisitor7<PluginEntry, Plugin> {
        private final Elements elements;

        private PluginElementVisitor(Elements elements) {
            this.elements = elements;
        }

        @Override
        public PluginEntry visitType(TypeElement e, Plugin plugin) {
            Objects.requireNonNull(plugin, "Plugin annotation is null.");
            PluginEntry entry = new PluginEntry();
            entry.setKey(Strings.toRootLowerCase(plugin.name()));
            entry.setClassName(this.elements.getBinaryName(e).toString());
            entry.setName("".equals(plugin.elementType()) ? plugin.name() : plugin.elementType());
            entry.setPrintable(plugin.printObject());
            entry.setDefer(plugin.deferChildren());
            entry.setCategory(plugin.category());
            return entry;
        }
    }

    private static final class PluginAliasesElementVisitor
    extends SimpleElementVisitor7<Collection<PluginEntry>, Plugin> {
        private final Elements elements;

        private PluginAliasesElementVisitor(Elements elements) {
            super(Collections.emptyList());
            this.elements = elements;
        }

        @Override
        public Collection<PluginEntry> visitType(TypeElement e, Plugin plugin) {
            PluginAliases aliases = e.getAnnotation(PluginAliases.class);
            if (aliases == null) {
                return (Collection)this.DEFAULT_VALUE;
            }
            ArrayList<PluginEntry> entries = new ArrayList<PluginEntry>(aliases.value().length);
            for (String alias : aliases.value()) {
                PluginEntry entry = new PluginEntry();
                entry.setKey(Strings.toRootLowerCase(alias));
                entry.setClassName(this.elements.getBinaryName(e).toString());
                entry.setName("".equals(plugin.elementType()) ? alias : plugin.elementType());
                entry.setPrintable(plugin.printObject());
                entry.setDefer(plugin.deferChildren());
                entry.setCategory(plugin.category());
                entries.add(entry);
            }
            return entries;
        }
    }
}

