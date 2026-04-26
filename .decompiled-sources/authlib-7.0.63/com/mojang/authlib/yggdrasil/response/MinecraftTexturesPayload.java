/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.util.Map;
import java.util.UUID;

public record MinecraftTexturesPayload(@SerializedName(value="timestamp") long timestamp, @SerializedName(value="profileId") UUID profileId, @SerializedName(value="profileName") String profileName, @SerializedName(value="isPublic") boolean isPublic, @SerializedName(value="textures") Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
}

