/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.blockgetter.v2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public interface FabricBlockGetter {
    default public @Nullable Object getBlockEntityRenderData(BlockPos pos) {
        BlockEntity blockEntity = ((BlockGetter)this).getBlockEntity(pos);
        return blockEntity == null ? null : blockEntity.getRenderData();
    }

    default public boolean hasBiomes() {
        return false;
    }

    default public @UnknownNullability Holder<Biome> getBiomeFabric(BlockPos pos) {
        return null;
    }
}

