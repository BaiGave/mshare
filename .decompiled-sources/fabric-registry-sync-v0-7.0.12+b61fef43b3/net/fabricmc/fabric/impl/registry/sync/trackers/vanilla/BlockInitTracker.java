/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import java.util.List;
import net.fabricmc.fabric.mixin.registry.sync.DebugLevelSourceAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockInitTracker {
    public static void postFreeze() {
        List<BlockState> blockStateList = BuiltInRegistries.BLOCK.stream().flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).toList();
        int xLength = Mth.ceil(Mth.sqrt(blockStateList.size()));
        int zLength = Mth.ceil((float)blockStateList.size() / (float)xLength);
        DebugLevelSourceAccessor.setALL_BLOCKS(blockStateList);
        DebugLevelSourceAccessor.setGRID_WIDTH(xLength);
        DebugLevelSourceAccessor.setGRID_HEIGHT(zLength);
    }
}

