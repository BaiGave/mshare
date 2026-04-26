/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.renderstate;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={WeatherRenderState.class})
abstract class WeatherRenderStateMixin {
    WeatherRenderStateMixin() {
    }

    @Inject(method={"reset"}, at={@At(value="TAIL")})
    private void clearExtraRenderData(CallbackInfo ci) {
        ((FabricRenderState)((Object)this)).clearExtraData();
    }
}

