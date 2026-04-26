/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.yggdrasil.response;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.ProfileAction;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public record MinecraftProfilePropertiesResponse(@SerializedName(value="id") UUID id, @SerializedName(value="name") String name, @SerializedName(value="properties") PropertyMap properties, @SerializedName(value="profileActions") @Nullable Set<ProfileAction> profileActions) {
    @SerializedName(value="profileActions")
    @Nullable
    private final Set<ProfileAction> profileActions;

    public GameProfile profile() {
        return new GameProfile(this.id, this.name, this.properties);
    }

    public Set<ProfileAction> profileActions() {
        return this.profileActions != null ? this.profileActions : Set.of();
    }
}

