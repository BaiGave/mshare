/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.api;

import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;

public interface TrMember {
    default public boolean isField() {
        return this.getType() == MemberType.FIELD;
    }

    default public boolean isMethod() {
        return this.getType() == MemberType.METHOD;
    }

    public MemberType getType();

    public TrClass getOwner();

    public String getName();

    public String getDesc();

    public int getAccess();

    public int getIndex();

    default public boolean isPublic() {
        return (this.getAccess() & 1) != 0;
    }

    default public boolean isProtected() {
        return (this.getAccess() & 4) != 0;
    }

    default public boolean isPrivate() {
        return (this.getAccess() & 2) != 0;
    }

    default public boolean isStatic() {
        return (this.getAccess() & 8) != 0;
    }

    default public boolean isSynthetic() {
        return (this.getAccess() & 0x1000) != 0;
    }

    public static enum MemberType {
        METHOD,
        FIELD;

    }
}

