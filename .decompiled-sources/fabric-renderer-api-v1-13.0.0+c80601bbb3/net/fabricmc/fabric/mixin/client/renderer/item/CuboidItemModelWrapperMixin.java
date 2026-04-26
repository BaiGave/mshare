/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CuboidItemModelWrapper.class})
abstract class CuboidItemModelWrapperMixin
implements ItemModel {
    @Shadow
    @Final
    private QuadCollection quads;

    CuboidItemModelWrapperMixin() {
    }

    @Inject(method={"update"}, at={@At(value="RETURN")})
    private void onReturnUpdate(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed, CallbackInfo ci, @Local(name={"layer"}) ItemStackRenderState.LayerRenderState layer) {
        QuadCollection quadCollection = this.quads;
        if (quadCollection instanceof MeshQuadCollection) {
            MeshQuadCollection meshQuadCollection = (MeshQuadCollection)quadCollection;
            meshQuadCollection.getMesh().outputTo(layer.emitter());
        }
    }
}

