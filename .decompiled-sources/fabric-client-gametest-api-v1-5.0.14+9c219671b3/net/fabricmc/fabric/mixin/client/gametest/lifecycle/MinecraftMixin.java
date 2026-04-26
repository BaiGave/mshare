/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.lifecycle;

import net.fabricmc.fabric.impl.client.gametest.FabricClientGameTestRunner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftMixin {
    @Unique
    private boolean startedClientGametests = false;
    @Shadow
    private @Nullable Overlay overlay;

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        if (!this.startedClientGametests && this.overlay == null) {
            this.startedClientGametests = true;
            FabricClientGameTestRunner.start();
        }
    }
}

