/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.server;

import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Main.class})
public class MainMixin {
    @Inject(method={"main"}, at={@At(value="NEW", target="net/minecraft/server/dedicated/DedicatedServerSettings")}, cancellable=true)
    private static void main(String[] args, CallbackInfo info) {
        if (FabricDataGenHelper.ENABLED) {
            FabricDataGenHelper.run();
            info.cancel();
        }
    }
}

