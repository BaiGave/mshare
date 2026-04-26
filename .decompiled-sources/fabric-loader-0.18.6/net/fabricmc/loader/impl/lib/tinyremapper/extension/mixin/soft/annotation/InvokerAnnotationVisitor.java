/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation;

import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.StringUtility;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.MxMember;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.util.NamedMappable;
import org.objectweb.asm.AnnotationVisitor;

public class InvokerAnnotationVisitor
extends AnnotationVisitor {
    private final CommonData data;
    private final MxMember method;
    private final List<String> targets;
    private boolean isSoftTarget;

    public InvokerAnnotationVisitor(CommonData data, AnnotationVisitor delegate, MxMember method, List<String> targets) {
        super(589824, Objects.requireNonNull(delegate));
        this.data = Objects.requireNonNull(data);
        this.method = Objects.requireNonNull(method);
        this.targets = Objects.requireNonNull(targets);
    }

    @Override
    public void visit(String name, Object value) {
        if (name.equals("value")) {
            this.isSoftTarget = true;
            String methodName = Objects.requireNonNull((String)value);
            this.setAnnotationValue(methodName);
            return;
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnd() {
        String inferredName;
        if (!this.isSoftTarget && (inferredName = this.inferMethodName()) != null) {
            this.setAnnotationValue(inferredName);
        }
        super.visitEnd();
    }

    private void setAnnotationValue(String methodName) {
        super.visit("value", new NamedMappable(this.data, methodName, this.method.getDesc(), this.targets).result());
    }

    private String inferMethodName() {
        String prefix;
        if (this.method.getName().startsWith("new") || this.method.getName().startsWith("create")) {
            return null;
        }
        if (this.method.getName().startsWith("call")) {
            prefix = "call";
        } else if (this.method.getName().startsWith("invoke")) {
            prefix = "invoke";
        } else {
            throw new RuntimeException(String.format("%s does not start with call or invoke.", this.method.getName()));
        }
        return StringUtility.removeCamelPrefix(prefix, this.method.getName());
    }
}

