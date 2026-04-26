/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.indigo.renderer;

import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemFeatureRenderer.class})
abstract class ItemFeatureRendererMixin {
    @Unique
    private final AltItemRenderer altItemRenderer = new AltItemRenderer();

    ItemFeatureRendererMixin() {
    }

    @Inject(method={"renderSolid"}, at={@At(value="RETURN")})
    private void onReturnRenderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
        this.altItemRenderer.prepare(bufferSource, outlineBufferSource, false);
        for (FabricSubmitNodeCollection.ExtendedItemSubmit submit : nodeCollection.getExtendedItemSubmits()) {
            this.altItemRenderer.renderItem(submit);
        }
        this.altItemRenderer.clear();
    }

    @Inject(method={"renderTranslucent"}, at={@At(value="RETURN")})
    private void onReturnRenderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
        this.altItemRenderer.prepare(bufferSource, outlineBufferSource, true);
        for (FabricSubmitNodeCollection.ExtendedItemSubmit submit : nodeCollection.getExtendedItemSubmits()) {
            this.altItemRenderer.renderItem(submit);
        }
        this.altItemRenderer.clear();
    }
}

