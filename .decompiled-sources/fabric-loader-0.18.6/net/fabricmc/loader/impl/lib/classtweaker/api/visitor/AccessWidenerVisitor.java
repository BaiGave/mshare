/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api.visitor;

public interface AccessWidenerVisitor {
    default public void visitClass(AccessType access, boolean transitive) {
    }

    default public void visitMethod(String name, String descriptor, AccessType access, boolean transitive) {
    }

    default public void visitField(String name, String descriptor, AccessType access, boolean transitive) {
    }

    public static enum AccessType {
        ACCESSIBLE("accessible"),
        EXTENDABLE("extendable"),
        MUTABLE("mutable");

        private final String id;

        private AccessType(String id) {
            this.id = id;
        }

        public String toString() {
            return this.id;
        }
    }
}

