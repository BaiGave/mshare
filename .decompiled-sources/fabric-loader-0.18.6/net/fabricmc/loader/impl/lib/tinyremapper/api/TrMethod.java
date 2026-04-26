/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.api;

import net.fabricmc.loader.impl.lib.tinyremapper.api.TrLocal;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;

public interface TrMethod
extends TrMember {
    default public boolean isBridge() {
        return this.getType().equals((Object)TrMember.MemberType.METHOD) && (this.getAccess() & 0x40) != 0;
    }

    default public boolean isAbstract() {
        return this.getType().equals((Object)TrMember.MemberType.METHOD) && (this.getAccess() & 0x400) != 0;
    }

    default public boolean isVirtual() {
        return this.getType().equals((Object)TrMember.MemberType.METHOD) && (this.getAccess() & 0xA) == 0;
    }

    public TrLocal[] getLocals();
}

