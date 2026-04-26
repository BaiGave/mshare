/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/client/gui/render/GuiRenderer$Draw"})
interface DrawAccessor {
    @Accessor(value="pipeline")
    public RenderPipeline fabric$pipeline();

    @Accessor(value="indexCount")
    public int fabric$indexCount();
}

