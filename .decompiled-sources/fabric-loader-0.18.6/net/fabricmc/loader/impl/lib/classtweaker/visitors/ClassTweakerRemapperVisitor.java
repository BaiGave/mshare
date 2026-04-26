/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.visitors;

import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.AccessWidenerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.ClassTweakerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.visitors.AccessWidenerRemapperVisitor;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.commons.Remapper;

public final class ClassTweakerRemapperVisitor
implements ClassTweakerVisitor {
    private final ClassTweakerVisitor delegate;
    private final Remapper remapper;
    private final String fromNamespace;
    private final String toNamespace;

    public ClassTweakerRemapperVisitor(ClassTweakerVisitor delegate, Remapper remapper, String fromNamespace, String toNamespace) {
        this.delegate = delegate;
        this.remapper = remapper;
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
    }

    @Override
    public void visitHeader(String namespace) {
        if (!this.fromNamespace.equals(namespace)) {
            throw new IllegalArgumentException("Cannot remap access widener from namespace '" + namespace + "'. Expected: '" + this.fromNamespace + "'");
        }
        this.delegate.visitHeader(this.toNamespace);
    }

    @Override
    @Nullable
    public AccessWidenerVisitor visitAccessWidener(String owner) {
        AccessWidenerVisitor delegateAccessWidenerVisitor = this.delegate.visitAccessWidener(this.remapper.map(owner));
        if (delegateAccessWidenerVisitor == null) {
            return null;
        }
        return new AccessWidenerRemapperVisitor(delegateAccessWidenerVisitor, this.remapper, owner);
    }

    @Override
    public void visitInjectedInterface(String owner, String iface, boolean transitive) {
        String mappedIfaceDesc = this.remapper.mapSignature("L" + iface + ";", false);
        String mappedIface = mappedIfaceDesc.substring(1, mappedIfaceDesc.length() - 1);
        this.delegate.visitInjectedInterface(this.remapper.map(owner), mappedIface, transitive);
    }
}

