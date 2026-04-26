/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.client;

import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftMixin {
    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;getBackendDescription()Ljava/lang/String;")})
    private void main(CallbackInfo info) {
        if (FabricDataGenHelper.ENABLED) {
            FabricDataGenHelper.run();
            System.exit(0);
        }
    }
}

