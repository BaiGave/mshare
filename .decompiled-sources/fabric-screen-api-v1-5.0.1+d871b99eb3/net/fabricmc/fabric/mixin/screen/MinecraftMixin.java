/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
abstract class MinecraftMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-api-v1");
    @Unique
    private static final boolean DEBUG_SCREEN = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("fabric.debugScreen");
    @Shadow
    public Screen screen;
    @Shadow
    private Thread gameThread;
    @Unique
    private Screen tickingScreen;

    MinecraftMixin() {
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")})
    private void checkThreadOnDev(@Nullable Screen screen, CallbackInfo ci) {
        Thread currentThread = Thread.currentThread();
        if (DEBUG_SCREEN && currentThread != this.gameThread) {
            LOGGER.error("Attempted to set screen to \"{}\" outside the render thread (\"{}\"). This will likely follow a crash! Make sure to call setScreen on the render thread.", (Object)screen, (Object)currentThread.getName());
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;removed()V", shift=At.Shift.AFTER)})
    private void onScreenRemove(@Nullable Screen screen, CallbackInfo ci) {
        ScreenEvents.remove(this.screen).invoker().onRemove(this.screen);
    }

    @Inject(method={"destroy"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;removed()V", shift=At.Shift.AFTER)})
    private void onScreenRemoveBecauseStopping(CallbackInfo ci) {
        ScreenEvents.remove(this.screen).invoker().onRemove(this.screen);
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;tick()V")})
    private void beforeScreenTick(CallbackInfo ci) {
        this.tickingScreen = this.screen;
        ScreenEvents.beforeTick(this.tickingScreen).invoker().beforeTick(this.tickingScreen);
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;tick()V", shift=At.Shift.AFTER)})
    private void afterScreenTick(CallbackInfo ci) {
        ScreenEvents.afterTick(this.tickingScreen).invoker().afterTick(this.tickingScreen);
        this.tickingScreen = null;
    }

    @Inject(method={"doWorldLoad"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/LevelLoadingScreen;tick()V")})
    private void beforeLoadingScreenTick(CallbackInfo ci) {
        this.tickingScreen = this.screen;
        ScreenEvents.beforeTick(this.tickingScreen).invoker().beforeTick(this.tickingScreen);
    }

    @Inject(method={"doWorldLoad"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;renderFrame(Z)V")})
    private void afterLoadingScreenTick(CallbackInfo ci) {
        ScreenEvents.afterTick(this.tickingScreen).invoker().afterTick(this.tickingScreen);
        this.tickingScreen = null;
    }
}

