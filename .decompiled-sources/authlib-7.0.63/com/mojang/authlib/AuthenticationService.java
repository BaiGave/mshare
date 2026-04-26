/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;

public interface AuthenticationService {
    public MinecraftSessionService createMinecraftSessionService();

    public GameProfileRepository createProfileRepository();
}

