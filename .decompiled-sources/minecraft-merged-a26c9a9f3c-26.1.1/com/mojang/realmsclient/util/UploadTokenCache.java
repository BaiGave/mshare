/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class UploadTokenCache {
    private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<String>();

    public static String get(long realmId) {
        return (String)TOKEN_CACHE.get(realmId);
    }

    public static void invalidate(long realmId) {
        TOKEN_CACHE.remove(realmId);
    }

    public static void put(long realmId, @Nullable String token) {
        TOKEN_CACHE.put(realmId, token);
    }
}

