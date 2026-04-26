/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface FabricBlockStateModelSet {
    default public Material.Baked getParticleMaterial(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return ((BlockStateModelSet)this).get(state).particleMaterial(level, pos, state);
    }
}

