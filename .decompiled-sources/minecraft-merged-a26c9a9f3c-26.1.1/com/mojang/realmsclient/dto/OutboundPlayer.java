/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.util.UUIDTypeAdapter;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OutboundPlayer
implements ReflectionBasedSerialization {
    @SerializedName(value="name")
    public @Nullable String name;
    @SerializedName(value="uuid")
    @JsonAdapter(value=UUIDTypeAdapter.class)
    public @Nullable UUID uuid;
}

