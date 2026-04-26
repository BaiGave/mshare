/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WrapperUnbakedRootBlockStateModel
implements BlockStateModel.UnbakedRoot {
    protected BlockStateModel.UnbakedRoot wrapped;

    protected WrapperUnbakedRootBlockStateModel() {
    }

    protected WrapperUnbakedRootBlockStateModel(BlockStateModel.UnbakedRoot wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        return this.wrapped.bake(state, baker);
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        return this.wrapped.visualEqualityGroup(state);
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.wrapped.resolveDependencies(resolver);
    }
}

