/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome.modification;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class BuiltInResourceKeys {
    private static final HolderLookup.Provider vanillaRegistries = VanillaRegistries.createLookup();

    private BuiltInResourceKeys() {
    }

    public static boolean isBuiltinBiome(ResourceKey<Biome> key) {
        return BuiltInResourceKeys.biomeHolderGetter().get(key).isPresent();
    }

    public static HolderGetter<Biome> biomeHolderGetter() {
        return vanillaRegistries.lookupOrThrow(Registries.BIOME);
    }
}

