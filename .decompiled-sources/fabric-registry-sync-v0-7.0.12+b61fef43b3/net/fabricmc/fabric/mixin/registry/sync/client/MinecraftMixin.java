/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync.client;

import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.trackers.vanilla.BlockInitTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(at={@At(value="RETURN")}, method={"disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V"})
    public void disconnectAfter(Screen disconnectionScreen, boolean bl, boolean bl2, CallbackInfo ci) {
        try {
            MinecraftMixin.unmap();
        }
        catch (RemapException e) {
            LOGGER.warn("Failed to unmap Fabric registries!", e);
        }
    }

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Ljava/lang/Thread;currentThread()Ljava/lang/Thread;")})
    private void afterModInit(CallbackInfo ci) {
        LOGGER.debug("Freezing registries");
        BuiltInRegistries.bootStrap();
        BlockInitTracker.postFreeze();
        CreativeModeTabs.validate();
    }

    @Unique
    private static void unmap() throws RemapException {
        for (Identifier registryId : BuiltInRegistries.REGISTRY.keySet()) {
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(registryId);
            if (!(registry instanceof RemappableRegistry)) continue;
            ((RemappableRegistry)((Object)registry)).unmap();
        }
    }
}

