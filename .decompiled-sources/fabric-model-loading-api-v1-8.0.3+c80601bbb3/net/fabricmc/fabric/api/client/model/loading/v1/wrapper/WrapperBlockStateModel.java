/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class WrapperBlockStateModel
implements BlockStateModel {
    protected BlockStateModel wrapped;

    protected WrapperBlockStateModel() {
    }

    protected WrapperBlockStateModel(BlockStateModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.wrapped.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.wrapped.particleMaterial();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.wrapped.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag) {
        return this.wrapped.hasMaterialFlag(flag);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        this.wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.wrapped.createGeometryKey(level, pos, state, random);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.wrapped.particleMaterial(level, pos, state);
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.wrapped.materialFlags(level, pos, state, random);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag) {
        return this.wrapped.hasMaterialFlag(level, pos, state, random, flag);
    }
}

