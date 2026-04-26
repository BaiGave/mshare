/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.debug.client;

import java.util.List;
import net.fabricmc.fabric.impl.debug.client.renderer.DebugRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DebugRenderer.class})
abstract class DebugRendererMixin {
    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> renderers;

    private DebugRendererMixin() {
    }

    @Inject(method={"refreshRendererList"}, at={@At(value="RETURN")})
    private void registerRenderers(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        for (DebugRendererRegistryImpl.Entry entry : DebugRendererRegistryImpl.RENDERERS) {
            this.renderers.add(entry.rendererFactory().create(minecraft));
        }
    }
}

