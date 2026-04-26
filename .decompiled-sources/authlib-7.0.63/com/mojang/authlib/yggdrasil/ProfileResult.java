/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import java.util.Set;

public record ProfileResult(GameProfile profile, Set<ProfileActionType> actions) {
    public ProfileResult(GameProfile profile) {
        this(profile, Set.of());
    }
}

