/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.annotation;

import java.util.List;
import java.util.Objects;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

public class MixinAnnotationVisitor
extends AnnotationVisitor {
    private final List<String> targets;

    public MixinAnnotationVisitor(AnnotationVisitor delegate, List<String> targetsOut) {
        super(589824, delegate);
        this.targets = Objects.requireNonNull(targetsOut);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor visitor = super.visitArray(name);
        if (name.equals("targets")) {
            return new AnnotationVisitor(589824, visitor){

                @Override
                public void visit(String name, Object value) {
                    String srcName;
                    String dstName = srcName = ((String)value).replaceAll("\\s", "").replace('.', '/');
                    MixinAnnotationVisitor.this.targets.add(srcName);
                    value = dstName;
                    super.visit(name, value);
                }
            };
        }
        if (name.equals("value")) {
            return new AnnotationVisitor(589824, visitor){

                @Override
                public void visit(String name, Object value) {
                    Type srcType = Objects.requireNonNull((Type)value);
                    MixinAnnotationVisitor.this.targets.add(srcType.getInternalName());
                    super.visit(name, value);
                }
            };
        }
        return visitor;
    }
}

