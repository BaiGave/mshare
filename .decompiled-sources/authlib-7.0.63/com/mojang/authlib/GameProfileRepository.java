/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib;

import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import java.util.Optional;

public interface GameProfileRepository {
    public void findProfilesByNames(String[] var1, ProfileLookupCallback var2);

    public Optional<NameAndId> findProfileByName(String var1);
}

