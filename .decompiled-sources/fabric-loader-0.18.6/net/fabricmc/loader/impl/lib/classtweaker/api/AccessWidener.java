/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api;

import net.fabricmc.loader.impl.lib.classtweaker.utils.EntryTriple;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface AccessWidener {
    public Access getClassAccess();

    public Access getMethodAccess(EntryTriple var1);

    public Access getFieldAccess(EntryTriple var1);

    public Access getCanonicalConstructorAccess();

    @ApiStatus.NonExtendable
    public static interface Access {
        public boolean isAccessible();

        public boolean isExtendable();

        public boolean isMutable();

        default public boolean isChanged() {
            return this.isAccessible() || this.isExtendable() || this.isMutable();
        }

        public int apply(int var1, String var2, int var3);
    }
}

