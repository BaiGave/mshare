/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection;

import java.util.Objects;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.DefinitionAnnotationVisitor;
import org.objectweb.asm.AnnotationVisitor;

public class DefinitionsAnnotationVisitor
extends AnnotationVisitor {
    private final CommonData data;

    public DefinitionsAnnotationVisitor(CommonData data, AnnotationVisitor delegate) {
        super(589824, Objects.requireNonNull(delegate));
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor av = super.visitArray(name);
        if (name.equals("value")) {
            return new DefinitionRemappingVisitor(this.data, av);
        }
        return av;
    }

    private static class DefinitionRemappingVisitor
    extends AnnotationVisitor {
        private final CommonData data;

        DefinitionRemappingVisitor(CommonData data, AnnotationVisitor delegate) {
            super(589824, Objects.requireNonNull(delegate));
            this.data = Objects.requireNonNull(data);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            return new DefinitionAnnotationVisitor(this.data, super.visitAnnotation(name, descriptor));
        }
    }
}

