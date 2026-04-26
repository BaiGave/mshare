/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.impl.client.rendering.GuiRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.LevelRendererExtensions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
public class GameRendererMixin {
    @Shadow
    @Final
    private GuiRenderer guiRenderer;
    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void guiRendererReady(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, RenderBuffers renderBuffers, ModelManager modelManager, CallbackInfo ci) {
        GuiRendererExtensions guiRenderer = (GuiRendererExtensions)((Object)this.guiRenderer);
        guiRenderer.fabric_onReady(this.submitNodeStorage);
    }

    @Inject(method={"extract"}, at={@At(value="HEAD")})
    private void beforeExtract(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        ((LevelRendererExtensions)((Object)this.minecraft.levelRenderer)).fabric_prepareLevelExtractionContext(deltaTracker);
    }
}

