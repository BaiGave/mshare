/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib.minecraft;

import com.google.gson.annotations.SerializedName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class MinecraftProfileTexture {
    public static final int PROFILE_TEXTURE_COUNT = Type.values().length;
    @SerializedName(value="url")
    private final String url;
    @SerializedName(value="metadata")
    private final Map<String, String> metadata;

    public MinecraftProfileTexture(String url, Map<String, String> metadata) {
        this.url = url;
        this.metadata = metadata;
    }

    public String getUrl() {
        return this.url;
    }

    @Nullable
    public String getMetadata(String key) {
        if (this.metadata == null) {
            return null;
        }
        return this.metadata.get(key);
    }

    public String getHash() {
        try {
            return FilenameUtils.getBaseName(new URL(this.url).getPath());
        }
        catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid profile texture url");
        }
    }

    public String toString() {
        return new ToStringBuilder(this).append("url", this.url).append("hash", this.getHash()).toString();
    }

    public static enum Type {
        SKIN,
        CAPE,
        ELYTRA;

    }
}

