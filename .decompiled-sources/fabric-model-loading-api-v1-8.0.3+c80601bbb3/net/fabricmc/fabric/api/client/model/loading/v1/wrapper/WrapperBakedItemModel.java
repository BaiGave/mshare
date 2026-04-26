/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class WrapperBakedItemModel
implements ItemModel {
    protected ItemModel wrapped;

    protected WrapperBakedItemModel() {
    }

    protected WrapperBakedItemModel(ItemModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner itemOwner, int seed) {
        this.wrapped.update(state, stack, resolver, displayContext, level, itemOwner, seed);
    }
}

