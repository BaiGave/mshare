/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.annotation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.MxMember;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util.ConvertibleMappable;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util.IConvertibleString;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util.IdentityString;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util.PrefixString;
import org.objectweb.asm.AnnotationVisitor;

public class ShadowAnnotationVisitor
extends AnnotationVisitor {
    private final Collection<Consumer<CommonData>> tasks;
    private final MxMember member;
    private final List<String> targets;
    private String prefix;

    public ShadowAnnotationVisitor(Collection<Consumer<CommonData>> tasks, AnnotationVisitor delegate, MxMember member, List<String> targets) {
        super(589824, delegate);
        this.tasks = Objects.requireNonNull(tasks);
        this.member = Objects.requireNonNull(member);
        this.targets = Objects.requireNonNull(targets);
        this.prefix = "shadow$";
    }

    @Override
    public void visit(String name, Object value) {
        if (name.equals("prefix")) {
            this.prefix = Objects.requireNonNull((String)value);
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnd() {
        this.tasks.add(data -> new ShadowPrefixMappable((CommonData)data, this.member, (Collection<String>)this.targets, this.prefix).result());
        super.visitEnd();
    }

    private static class ShadowPrefixMappable
    extends ConvertibleMappable {
        private final String prefix;

        ShadowPrefixMappable(CommonData data, MxMember self, Collection<String> targets, String prefix) {
            super(data, self, targets);
            Objects.requireNonNull(prefix);
            this.prefix = self.getName().startsWith(prefix) ? prefix : "";
        }

        @Override
        protected IConvertibleString getName() {
            if (this.prefix.isEmpty()) {
                return new IdentityString(this.self.getName());
            }
            return new PrefixString(this.prefix, this.self.getName());
        }

        @Override
        protected String getDesc() {
            return this.self.getDesc();
        }
    }
}

