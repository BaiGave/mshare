/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.mixin.client.renderer.block.model.MultiPartModelSharedBakedStateAccessor;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={MultiPartModel.class})
abstract class MultiPartModelMixin
implements BlockStateModel {
    @Shadow
    @Final
    private MultiPartModel.SharedBakedState shared;
    @Shadow
    @Final
    private BlockState blockState;
    @Shadow
    private @Nullable List<BlockStateModel> models;

    MultiPartModelMixin() {
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        if (this.models == null) {
            this.models = this.shared.selectModels(this.blockState);
        }
        long seed = random.nextLong();
        for (BlockStateModel model : this.models) {
            random.setSeed(seed);
            model.emitQuads(emitter, level, pos, state, random, cullTest);
        }
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (this.models == null) {
            this.models = this.shared.selectModels(this.blockState);
        }
        int count = this.models.size();
        long seed = random.nextLong();
        if (count == 1) {
            random.setSeed(seed);
            return this.models.getFirst().createGeometryKey(level, pos, state, random);
        }
        ArrayList<Object> subkeys = new ArrayList<Object>(count);
        for (int i = 0; i < count; ++i) {
            random.setSeed(seed);
            Object subkey = this.models.get(i).createGeometryKey(level, pos, state, random);
            if (subkey == null) {
                return null;
            }
            subkeys.add(subkey);
        }
        record Key(List<Object> subkeys) {
        }
        return new Key(subkeys);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return ((MultiPartModelSharedBakedStateAccessor)((Object)this.shared)).getSelectors().getFirst().model().particleMaterial(level, pos, state);
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (this.models == null) {
            this.models = this.shared.selectModels(this.blockState);
        }
        long seed = random.nextLong();
        int flags = 0;
        for (BlockStateModel model : this.models) {
            random.setSeed(seed);
            flags |= model.materialFlags(level, pos, state, random);
        }
        return flags;
    }
}

