/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import java.util.Objects;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperBlocks;

public final class OxidizableBlocksRegistryImpl {
    private OxidizableBlocksRegistryImpl() {
    }

    public static void registerNextStage(Block from, Block to) {
        Objects.requireNonNull(from, "Oxidizable block cannot be null!");
        Objects.requireNonNull(to, "Oxidizable block cannot be null!");
        WeatheringCopper.NEXT_BY_BLOCK.get().put(from, to);
        OxidizableBlocksRegistryImpl.refreshRandomTickCache(from);
        OxidizableBlocksRegistryImpl.refreshRandomTickCache(to);
    }

    public static void registerWaxable(Block unwaxed, Block waxed) {
        Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
        Objects.requireNonNull(waxed, "Waxed block cannot be null!");
        HoneycombItem.WAXABLES.get().put(unwaxed, waxed);
    }

    public static void registerWeatheringCopperBlocks(WeatheringCopperBlocks copperBlocks) {
        Objects.requireNonNull(copperBlocks, "copperBlocks cannot be null!");
        copperBlocks.weatheringMapping().forEach(OxidizableBlocksRegistryImpl::registerNextStage);
        copperBlocks.waxedMapping().forEach(OxidizableBlocksRegistryImpl::registerWaxable);
    }

    private static void refreshRandomTickCache(Block block) {
        block.getStateDefinition().getPossibleStates().forEach(state -> ((RandomTickCacheRefresher)((Object)state)).fabric_api$refreshRandomTickCache());
    }

    public static interface RandomTickCacheRefresher {
        public void fabric_api$refreshRandomTickCache();
    }
}

