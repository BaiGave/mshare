/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.impl;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.impl.lib.classtweaker.api.AccessWidener;
import net.fabricmc.loader.impl.lib.classtweaker.api.visitor.AccessWidenerVisitor;
import net.fabricmc.loader.impl.lib.classtweaker.utils.AccessUtils;
import net.fabricmc.loader.impl.lib.classtweaker.utils.EntryTriple;
import org.jetbrains.annotations.VisibleForTesting;

public final class AccessWidenerImpl
implements AccessWidener,
AccessWidenerVisitor {
    private final String owner;
    MutableAccess classAccess = ClassAccess.DEFAULT;
    final Map<EntryTriple, MutableAccess> methodAccess = new HashMap<EntryTriple, MutableAccess>();
    final Map<EntryTriple, MutableAccess> fieldAccess = new HashMap<EntryTriple, MutableAccess>();
    static final AccessWidener DEFAULT = new AccessWidener(){

        @Override
        public AccessWidener.Access getClassAccess() {
            return MutableAccess.DEFAULT;
        }

        @Override
        public AccessWidener.Access getMethodAccess(EntryTriple EntryTriple2) {
            return MutableAccess.DEFAULT;
        }

        @Override
        public AccessWidener.Access getFieldAccess(EntryTriple entryTriple) {
            return MutableAccess.DEFAULT;
        }

        @Override
        public AccessWidener.Access getCanonicalConstructorAccess() {
            return MutableAccess.DEFAULT;
        }
    };

    public AccessWidenerImpl(String owner) {
        this.owner = owner;
    }

    @Override
    public MutableAccess getClassAccess() {
        return this.classAccess;
    }

    @Override
    public AccessWidener.Access getMethodAccess(EntryTriple entryTriple) {
        AccessWidener.Access access = this.methodAccess.get(entryTriple);
        if (access == null) {
            return MutableAccess.DEFAULT;
        }
        return access;
    }

    @Override
    public AccessWidener.Access getFieldAccess(EntryTriple entryTriple) {
        AccessWidener.Access access = this.fieldAccess.get(entryTriple);
        if (access == null) {
            return MutableAccess.DEFAULT;
        }
        return access;
    }

    @Override
    public AccessWidener.Access getCanonicalConstructorAccess() {
        if (this.classAccess.isAccessible()) {
            return MethodAccess.ACCESSIBLE;
        }
        return MethodAccess.DEFAULT;
    }

    @Override
    public void visitClass(AccessWidenerVisitor.AccessType access, boolean transitive) {
        this.classAccess = this.applyAccess(access, this.classAccess, null);
    }

    @Override
    public void visitMethod(String name, String descriptor, AccessWidenerVisitor.AccessType access, boolean transitive) {
        this.addOrMerge(this.methodAccess, new EntryTriple(this.owner, name, descriptor), access, MethodAccess.DEFAULT);
    }

    @Override
    public void visitField(String name, String descriptor, AccessWidenerVisitor.AccessType access, boolean transitive) {
        this.addOrMerge(this.fieldAccess, new EntryTriple(this.owner, name, descriptor), access, FieldAccess.DEFAULT);
    }

    MutableAccess applyAccess(AccessWidenerVisitor.AccessType input, MutableAccess access, EntryTriple entryTriple) {
        switch (input) {
            case ACCESSIBLE: {
                this.makeClassAccessible(entryTriple);
                return access.makeAccessible();
            }
            case EXTENDABLE: {
                this.makeClassExtendable(entryTriple);
                return access.makeExtendable();
            }
            case MUTABLE: {
                return access.makeMutable();
            }
        }
        throw new UnsupportedOperationException("Unknown access type:" + (Object)((Object)input));
    }

    private void makeClassAccessible(EntryTriple entryTriple) {
        if (entryTriple == null) {
            return;
        }
        this.classAccess = this.applyAccess(AccessWidenerVisitor.AccessType.ACCESSIBLE, this.classAccess, null);
    }

    private void makeClassExtendable(EntryTriple entryTriple) {
        if (entryTriple == null) {
            return;
        }
        this.classAccess = this.applyAccess(AccessWidenerVisitor.AccessType.EXTENDABLE, this.classAccess, null);
    }

    void addOrMerge(Map<EntryTriple, MutableAccess> map, EntryTriple entry, AccessWidenerVisitor.AccessType access, MutableAccess defaultAccess) {
        if (entry == null || access == null) {
            throw new RuntimeException("Input entry or access is null");
        }
        map.put(entry, this.applyAccess(access, map.getOrDefault(entry, defaultAccess), entry));
    }

    @VisibleForTesting
    public static enum ClassAccess implements MutableAccess
    {
        DEFAULT((access, name, ownerAccess) -> access),
        ACCESSIBLE((access, name, ownerAccess) -> AccessUtils.makePublic(access)),
        EXTENDABLE((access, name, ownerAccess) -> AccessUtils.makePublic(AccessUtils.removeFinal(access))),
        ACCESSIBLE_EXTENDABLE((access, name, ownerAccess) -> AccessUtils.makePublic(AccessUtils.removeFinal(access)));

        private final AccessOperator operator;

        private ClassAccess(AccessOperator operator) {
            this.operator = operator;
        }

        @Override
        public MutableAccess makeAccessible() {
            if (this == EXTENDABLE || this == ACCESSIBLE_EXTENDABLE) {
                return ACCESSIBLE_EXTENDABLE;
            }
            return ACCESSIBLE;
        }

        @Override
        public MutableAccess makeExtendable() {
            if (this.isAccessible()) {
                return ACCESSIBLE_EXTENDABLE;
            }
            return EXTENDABLE;
        }

        @Override
        public MutableAccess makeMutable() {
            throw new UnsupportedOperationException("Classes cannot be made mutable");
        }

        @Override
        public boolean isAccessible() {
            return this == ACCESSIBLE || this == ACCESSIBLE_EXTENDABLE;
        }

        @Override
        public boolean isExtendable() {
            return this == EXTENDABLE || this == ACCESSIBLE_EXTENDABLE;
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public int apply(int access, String targetName, int ownerAccess) {
            return this.operator.apply(access, targetName, ownerAccess);
        }
    }

    static interface MutableAccess
    extends AccessWidener.Access {
        public static final AccessWidener.Access DEFAULT = new AccessWidener.Access(){

            @Override
            public boolean isAccessible() {
                return false;
            }

            @Override
            public boolean isExtendable() {
                return false;
            }

            @Override
            public boolean isMutable() {
                return false;
            }

            @Override
            public int apply(int access, String targetName, int ownerAccess) {
                return access;
            }
        };

        public MutableAccess makeAccessible();

        public MutableAccess makeExtendable();

        public MutableAccess makeMutable();
    }

    @VisibleForTesting
    public static enum MethodAccess implements MutableAccess
    {
        DEFAULT((access, name, ownerAccess) -> access),
        ACCESSIBLE((access, name, ownerAccess) -> AccessUtils.makePublic(AccessUtils.makeFinalIfPrivate(access, name, ownerAccess))),
        EXTENDABLE((access, name, ownerAccess) -> AccessUtils.makeProtected(AccessUtils.removeFinal(access))),
        ACCESSIBLE_EXTENDABLE((access, name, owner) -> AccessUtils.makePublic(AccessUtils.removeFinal(access)));

        private final AccessOperator operator;

        private MethodAccess(AccessOperator operator) {
            this.operator = operator;
        }

        @Override
        public MutableAccess makeAccessible() {
            if (this.isExtendable()) {
                return ACCESSIBLE_EXTENDABLE;
            }
            return ACCESSIBLE;
        }

        @Override
        public MutableAccess makeExtendable() {
            if (this.isAccessible()) {
                return ACCESSIBLE_EXTENDABLE;
            }
            return EXTENDABLE;
        }

        @Override
        public MutableAccess makeMutable() {
            throw new UnsupportedOperationException("Methods cannot be made mutable");
        }

        @Override
        public boolean isAccessible() {
            return this == ACCESSIBLE || this == ACCESSIBLE_EXTENDABLE;
        }

        @Override
        public boolean isExtendable() {
            return this == EXTENDABLE || this == ACCESSIBLE_EXTENDABLE;
        }

        @Override
        public boolean isMutable() {
            return false;
        }

        @Override
        public int apply(int access, String targetName, int ownerAccess) {
            return this.operator.apply(access, targetName, ownerAccess);
        }
    }

    @VisibleForTesting
    public static enum FieldAccess implements MutableAccess
    {
        DEFAULT((access, name, ownerAccess) -> access),
        ACCESSIBLE((access, name, ownerAccess) -> AccessUtils.makePublic(access)),
        MUTABLE((access, name, ownerAccess) -> {
            if ((ownerAccess & 0x200) != 0 && (access & 8) != 0) {
                return access;
            }
            return AccessUtils.removeFinal(access);
        }),
        ACCESSIBLE_MUTABLE((access, name, ownerAccess) -> {
            if ((ownerAccess & 0x200) != 0 && (access & 8) != 0) {
                return AccessUtils.makePublic(access);
            }
            return AccessUtils.makePublic(AccessUtils.removeFinal(access));
        });

        private final AccessOperator operator;

        private FieldAccess(AccessOperator operator) {
            this.operator = operator;
        }

        @Override
        public MutableAccess makeAccessible() {
            if (this.isMutable()) {
                return ACCESSIBLE_MUTABLE;
            }
            return ACCESSIBLE;
        }

        @Override
        public MutableAccess makeExtendable() {
            throw new UnsupportedOperationException("Fields cannot be made extendable");
        }

        @Override
        public MutableAccess makeMutable() {
            if (this.isAccessible()) {
                return ACCESSIBLE_MUTABLE;
            }
            return MUTABLE;
        }

        @Override
        public boolean isAccessible() {
            return this == ACCESSIBLE || this == ACCESSIBLE_MUTABLE;
        }

        @Override
        public boolean isExtendable() {
            return false;
        }

        @Override
        public boolean isMutable() {
            return this == MUTABLE || this == ACCESSIBLE_MUTABLE;
        }

        @Override
        public int apply(int access, String targetName, int ownerAccess) {
            return this.operator.apply(access, targetName, ownerAccess);
        }
    }

    @FunctionalInterface
    public static interface AccessOperator {
        public int apply(int var1, String var2, int var3);
    }
}

