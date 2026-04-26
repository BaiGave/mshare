/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import org.joml.Matrix4fc;

public abstract class WrapperUnbakedItemModel
implements ItemModel.Unbaked {
    protected ItemModel.Unbaked wrapped;

    protected WrapperUnbakedItemModel() {
    }

    protected WrapperUnbakedItemModel(ItemModel.Unbaked wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        this.wrapped.resolveDependencies(resolver);
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
        return this.wrapped.type();
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
        return this.wrapped.bake(context, transformation);
    }
}

