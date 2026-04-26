/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

public final class Annotations {
    private static final Collection<String> PARAMETER_ANNOTATION_NAMES = Arrays.asList("org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute", "org.apache.logging.log4j.core.config.plugins.PluginConfiguration", "org.apache.logging.log4j.core.config.plugins.PluginElement", "org.apache.logging.log4j.core.config.plugins.PluginLoggerContext", "org.apache.logging.log4j.core.config.plugins.PluginNode", "org.apache.logging.log4j.core.config.plugins.PluginValue");
    private static final Collection<String> FACTORY_ANNOTATION_NAMES = Arrays.asList("org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory", "org.apache.logging.log4j.core.config.plugins.PluginFactory");
    private static final Collection<String> PLUGIN_ANNOTATION_NAMES = Collections.singletonList("org.apache.logging.log4j.core.config.plugins.Plugin");
    private static final Collection<String> CONSTRAINT_OR_VISITOR_ANNOTATION_NAMES = Arrays.asList("org.apache.logging.log4j.core.config.plugins.validation.Constraint", "org.apache.logging.log4j.core.config.plugins.PluginVisitorStrategy");
    private final Map<TypeElement, Type> typeElementToTypeMap = new HashMap<TypeElement, Type>();

    public Annotations(Elements elements) {
        PARAMETER_ANNOTATION_NAMES.forEach(className -> this.addTypeElementIfExists(elements, (CharSequence)className, Type.PARAMETER));
        FACTORY_ANNOTATION_NAMES.forEach(className -> this.addTypeElementIfExists(elements, (CharSequence)className, Type.FACTORY));
        PLUGIN_ANNOTATION_NAMES.forEach(className -> this.addTypeElementIfExists(elements, (CharSequence)className, Type.PLUGIN));
        CONSTRAINT_OR_VISITOR_ANNOTATION_NAMES.forEach(className -> this.addTypeElementIfExists(elements, (CharSequence)className, Type.CONSTRAINT_OR_VISITOR));
    }

    private void addTypeElementIfExists(Elements elements, CharSequence className, Type type) {
        TypeElement element = elements.getTypeElement(className);
        if (element != null) {
            this.typeElementToTypeMap.put(element, type);
        }
    }

    public Type classifyAnnotation(TypeElement element) {
        return this.typeElementToTypeMap.getOrDefault(element, Type.UNKNOWN);
    }

    public Element getAnnotationClassValue(Element element, TypeElement annotation) {
        AnnotationMirror annotationMirror = element.getAnnotationMirrors().stream().filter(am -> am.getAnnotationType().asElement().equals(annotation)).findFirst().orElseThrow(() -> new IllegalStateException("No `@" + annotation + "` annotation found on " + element));
        AnnotationValue annotationValue = annotationMirror.getElementValues().entrySet().stream().filter(e -> "value".equals(((ExecutableElement)e.getKey()).getSimpleName().toString())).map(Map.Entry::getValue).findFirst().orElseThrow(() -> new IllegalStateException("No `value` found `@" + annotation + "` annotation on " + element));
        DeclaredType value = (DeclaredType)annotationValue.getValue();
        return value.asElement();
    }

    public static enum Type {
        PARAMETER,
        FACTORY,
        PLUGIN,
        CONSTRAINT_OR_VISITOR,
        UNKNOWN;

    }
}

