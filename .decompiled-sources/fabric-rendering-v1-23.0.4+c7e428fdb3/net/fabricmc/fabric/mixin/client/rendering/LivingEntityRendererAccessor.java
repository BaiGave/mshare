/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={LivingEntityRenderer.class})
public interface LivingEntityRendererAccessor<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Invoker
    public boolean callAddLayer(RenderLayer<S, M> var1);
}

