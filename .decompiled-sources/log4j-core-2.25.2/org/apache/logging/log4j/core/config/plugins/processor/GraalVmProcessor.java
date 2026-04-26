/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor;

import aQute.bnd.annotation.spi.ServiceProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import org.apache.logging.log4j.core.config.plugins.processor.internal.Annotations;
import org.apache.logging.log4j.core.config.plugins.processor.internal.ReachabilityMetadata;
import org.jspecify.annotations.Nullable;

@SupportedAnnotationTypes(value={"org.apache.logging.log4j.core.config.plugins.validation.Constraint", "org.apache.logging.log4j.core.config.plugins.Plugin", "org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute", "org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory", "org.apache.logging.log4j.core.config.plugins.PluginConfiguration", "org.apache.logging.log4j.core.config.plugins.PluginElement", "org.apache.logging.log4j.core.config.plugins.PluginFactory", "org.apache.logging.log4j.core.config.plugins.PluginLoggerContext", "org.apache.logging.log4j.core.config.plugins.PluginNode", "org.apache.logging.log4j.core.config.plugins.PluginValue", "org.apache.logging.log4j.core.config.plugins.PluginVisitorStrategy"})
@SupportedOptions(value={"log4j.graalvm.groupId", "log4j.graalvm.artifactId"})
@ServiceProvider(value=Processor.class, resolution="optional")
public class GraalVmProcessor
extends AbstractProcessor {
    static final String GROUP_ID = "log4j.graalvm.groupId";
    static final String ARTIFACT_ID = "log4j.graalvm.artifactId";
    private static final String LOCATION_PREFIX = "META-INF/native-image/log4j-generated/";
    private static final String LOCATION_SUFFIX = "/reflect-config.json";
    private static final String PROCESSOR_NAME = GraalVmProcessor.class.getSimpleName();
    private final Map<String, ReachabilityMetadata.Type> reachableTypes = new HashMap<String, ReachabilityMetadata.Type>();
    private final List<Element> processedElements = new ArrayList<Element>();
    private Annotations annotationUtil;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.annotationUtil = new Annotations(processingEnv.getElementUtils());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = this.processingEnv.getMessager();
        for (TypeElement typeElement : annotations) {
            Annotations.Type annotationType = this.annotationUtil.classifyAnnotation(typeElement);
            for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                switch (annotationType) {
                    case PLUGIN: {
                        this.processPlugin(element);
                        break;
                    }
                    case CONSTRAINT_OR_VISITOR: {
                        this.processConstraintOrVisitor(element, typeElement);
                        break;
                    }
                    case PARAMETER: {
                        this.processParameter(element);
                        break;
                    }
                    case FACTORY: {
                        this.processFactory(element);
                        break;
                    }
                    case UNKNOWN: {
                        messager.printMessage(Diagnostic.Kind.WARNING, String.format("The annotation type `%s` is not handled by %s", typeElement, PROCESSOR_NAME), typeElement);
                    }
                }
                this.processedElements.add(element);
            }
        }
        if (roundEnv.processingOver() && !this.reachableTypes.isEmpty()) {
            this.writeReachabilityMetadata();
        }
        return false;
    }

    private void processPlugin(Element element) {
        TypeElement typeElement = this.safeCast(element, TypeElement.class);
        for (Element element2 : typeElement.getEnclosedElements()) {
            ExecutableElement executableChild;
            if (!(element2 instanceof ExecutableElement) || !(executableChild = (ExecutableElement)element2).getModifiers().contains((Object)Modifier.PUBLIC)) continue;
            switch (executableChild.getSimpleName().toString()) {
                case "<init>": {
                    this.addMethod(typeElement, executableChild);
                    break;
                }
                case "newInstance": {
                    if (!executableChild.getModifiers().contains((Object)Modifier.STATIC)) break;
                    this.addMethod(typeElement, executableChild);
                    break;
                }
            }
        }
    }

    private void processConstraintOrVisitor(Element element, TypeElement annotation) {
        this.processPlugin(this.annotationUtil.getAnnotationClassValue(element, annotation));
    }

    private void processParameter(Element element) {
        switch (element.getKind()) {
            case FIELD: {
                this.addField(this.safeCast(element.getEnclosingElement(), TypeElement.class), this.safeCast(element, VariableElement.class));
                break;
            }
            case PARAMETER: {
                break;
            }
            default: {
                String msg = String.format("Invalid Log4j parameter element `%s`.", element);
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
                throw new IllegalStateException(msg);
            }
        }
    }

    private void processFactory(Element element) {
        this.addMethod(this.safeCast(element.getEnclosingElement(), TypeElement.class), this.safeCast(element, ExecutableElement.class));
    }

    private void writeReachabilityMetadata() {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            ReachabilityMetadata.writeReflectConfig(this.reachableTypes.values(), arrayOutputStream);
        }
        catch (IOException e) {
            String message = String.format("%s: an error occurred while generating reachability metadata: %s", PROCESSOR_NAME, e.getMessage());
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
            return;
        }
        byte[] data = arrayOutputStream.toByteArray();
        Map<String, String> options = this.processingEnv.getOptions();
        String reachabilityMetadataPath = this.getReachabilityMetadataPath(options.get(GROUP_ID), options.get(ARTIFACT_ID), Integer.toHexString(Arrays.hashCode(data)));
        Messager messager = this.processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("%s: writing GraalVM metadata for %d Java classes to `%s`.", PROCESSOR_NAME, this.reachableTypes.size(), reachabilityMetadataPath));
        try (OutputStream output = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", reachabilityMetadataPath, this.processedElements.toArray(new Element[0])).openOutputStream();){
            output.write(data);
        }
        catch (IOException e) {
            String message = String.format("%s: unable to write reachability metadata to file `%s`", PROCESSOR_NAME, reachabilityMetadataPath);
            messager.printMessage(Diagnostic.Kind.ERROR, message);
            throw new IllegalArgumentException(message, e);
        }
    }

    String getReachabilityMetadataPath(@Nullable String groupId, @Nullable String artifactId, String fallbackFolderName) {
        if (groupId == null || artifactId == null) {
            String message = String.format("The `%1$s` annotation processor is missing the recommended `%2$s` and `%3$s` options.%nTo follow the GraalVM recommendations, please add the following options to your build tool:%n  -A%2$s=<groupId>%n  -A%3$s=<artifactId>%n", PROCESSOR_NAME, GROUP_ID, ARTIFACT_ID);
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
            return LOCATION_PREFIX + fallbackFolderName + LOCATION_SUFFIX;
        }
        return LOCATION_PREFIX + groupId + '/' + artifactId + LOCATION_SUFFIX;
    }

    private void addField(TypeElement parent, VariableElement element) {
        ReachabilityMetadata.Type reachableType = this.reachableTypes.computeIfAbsent(this.toString(parent), ReachabilityMetadata.Type::new);
        reachableType.addField(new ReachabilityMetadata.Field(element.getSimpleName().toString()));
    }

    private void addMethod(TypeElement parent, ExecutableElement element) {
        ReachabilityMetadata.Type reachableType = this.reachableTypes.computeIfAbsent(this.toString(parent), ReachabilityMetadata.Type::new);
        ReachabilityMetadata.Method method = new ReachabilityMetadata.Method(element.getSimpleName().toString());
        element.getParameters().stream().map(v -> this.toString(v.asType())).forEach(method::addParameterType);
        reachableType.addMethod(method);
    }

    private <T extends Element> T safeCast(Element element, Class<T> type) {
        if (type.isInstance(element)) {
            return (T)((Element)type.cast(element));
        }
        String msg = String.format("Unexpected type of element `%s`: expecting `%s` but found `%s`", element, type.getName(), element.getClass().getName());
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
        throw new IllegalStateException(msg);
    }

    private String toString(TypeMirror type) {
        return type.accept(new SimpleTypeVisitor8<String, Void>(){

            @Override
            protected String defaultAction(TypeMirror e, @Nullable Void unused) {
                return e.toString();
            }

            @Override
            public String visitArray(ArrayType t, @Nullable Void unused) {
                return (String)this.visit(t.getComponentType(), unused) + "[]";
            }

            @Override
            public @Nullable String visitDeclared(DeclaredType t, Void unused) {
                return ((TypeElement)GraalVmProcessor.this.safeCast(t.asElement(), TypeElement.class)).getQualifiedName().toString();
            }
        }, null);
    }

    private String toString(Element element) {
        return element.accept(new SimpleElementVisitor8<String, Void>(){

            @Override
            public String visitPackage(PackageElement e, @Nullable Void unused) {
                return e.getQualifiedName().toString();
            }

            @Override
            public String visitType(TypeElement e, @Nullable Void unused) {
                Element parent = e.getEnclosingElement();
                String separator = parent.getKind() == ElementKind.PACKAGE ? "." : "$";
                return (String)this.visit(parent, unused) + separator + e.getSimpleName().toString();
            }

            @Override
            protected String defaultAction(Element e, @Nullable Void unused) {
                return "";
            }
        }, null);
    }
}

