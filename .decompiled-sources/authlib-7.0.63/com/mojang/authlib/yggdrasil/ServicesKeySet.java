/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface ServicesKeySet {
    public static final ServicesKeySet EMPTY = type -> List.of();

    public static ServicesKeySet lazy(Supplier<ServicesKeySet> supplier) {
        return type -> ((ServicesKeySet)supplier.get()).keys(type);
    }

    public Collection<ServicesKeyInfo> keys(ServicesKeyType var1);
}

