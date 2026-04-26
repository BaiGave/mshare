/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api.visitor;

import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.AccessWidenerVisitor;
import org.jetbrains.annotations.Nullable;

public interface ClassTweakerVisitor {
    default public void visitHeader(String namespace) {
    }

    @Nullable
    default public AccessWidenerVisitor visitAccessWidener(String owner) {
        return null;
    }

    default public void visitInjectedInterface(String owner, String iface, boolean transitive) {
    }

    default public void visitLineNumber(int lineNumber) {
    }
}

