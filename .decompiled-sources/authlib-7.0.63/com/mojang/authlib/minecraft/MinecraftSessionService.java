/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.net.InetAddress;
import java.util.UUID;
import javax.annotation.Nullable;

public interface MinecraftSessionService {
    public void joinServer(UUID var1, String var2, String var3) throws AuthenticationException;

    @Nullable
    public ProfileResult hasJoinedServer(String var1, String var2, @Nullable InetAddress var3) throws AuthenticationUnavailableException;

    @Nullable
    public Property getPackedTextures(GameProfile var1);

    public MinecraftProfileTextures unpackTextures(Property var1);

    default public MinecraftProfileTextures getTextures(GameProfile profile) {
        Property packed = this.getPackedTextures(profile);
        return packed != null ? this.unpackTextures(packed) : MinecraftProfileTextures.EMPTY;
    }

    @Nullable
    public ProfileResult fetchProfile(UUID var1, boolean var2);

    public String getSecurePropertyValue(Property var1) throws InsecurePublicKeyException;
}

