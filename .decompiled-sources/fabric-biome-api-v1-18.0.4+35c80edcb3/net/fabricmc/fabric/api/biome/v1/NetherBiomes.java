/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public final class NetherBiomes {
    private NetherBiomes() {
    }

    public static void addNetherBiome(ResourceKey<Biome> biome, Climate.TargetPoint targetPoint) {
        NetherBiomeData.addNetherBiome(biome, Climate.parameters(targetPoint.temperature(), targetPoint.humidity(), targetPoint.continentalness(), targetPoint.erosion(), targetPoint.depth(), targetPoint.weirdness(), 0.0f));
    }

    public static void addNetherBiome(ResourceKey<Biome> biome, Climate.ParameterPoint parameterPoint) {
        NetherBiomeData.addNetherBiome(biome, parameterPoint);
    }

    public static boolean canGenerateInNether(ResourceKey<Biome> biome) {
        return NetherBiomeData.canGenerateInNether(biome);
    }
}

