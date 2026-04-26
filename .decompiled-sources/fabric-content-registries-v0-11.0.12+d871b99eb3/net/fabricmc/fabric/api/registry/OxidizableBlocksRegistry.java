/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopperBlocks;

public final class OxidizableBlocksRegistry {
    private OxidizableBlocksRegistry() {
    }

    public static void registerNextStage(Block from, Block to) {
        OxidizableBlocksRegistryImpl.registerNextStage(from, to);
    }

    public static void registerWaxable(Block unwaxed, Block waxed) {
        OxidizableBlocksRegistryImpl.registerWaxable(unwaxed, waxed);
    }

    public static void registerWeatheringCopperBlocks(WeatheringCopperBlocks copperBlocks) {
        OxidizableBlocksRegistryImpl.registerWeatheringCopperBlocks(copperBlocks);
    }
}

