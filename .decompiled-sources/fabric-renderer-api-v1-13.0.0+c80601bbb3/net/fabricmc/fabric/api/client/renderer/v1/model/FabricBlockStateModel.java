/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import java.util.ArrayList;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface FabricBlockStateModel {
    default public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        boolean cutoutLeaves = Minecraft.getInstance().options.cutoutLeaves().get();
        boolean forceOpaque = ModelBlockRenderer.forceOpaque(cutoutLeaves, state);
        if (forceOpaque) {
            emitter.pushTransform(quad -> {
                quad.chunkLayer(ChunkSectionLayer.SOLID);
                return true;
            });
        }
        ArrayList<BlockStateModelPart> parts = new ArrayList<BlockStateModelPart>();
        ((BlockStateModel)this).collectParts(random, parts);
        int partCount = parts.size();
        for (int i = 0; i < partCount; ++i) {
            ((BlockStateModelPart)parts.get(i)).emitQuads(emitter, cullTest);
        }
        if (forceOpaque) {
            emitter.popTransform();
        }
    }

    default public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }

    default public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return ((BlockStateModel)this).particleMaterial();
    }

    @BakedQuad.MaterialFlags
    default public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ((BlockStateModel)this).materialFlags();
    }

    default public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag) {
        return (this.materialFlags(level, pos, state, random) & flag) != 0;
    }
}

