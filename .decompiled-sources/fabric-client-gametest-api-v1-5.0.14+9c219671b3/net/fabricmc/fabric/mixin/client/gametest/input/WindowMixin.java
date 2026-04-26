/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.GpuBackend;
import java.util.Optional;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Window.class})
public abstract class WindowMixin
implements WindowHooks {
    @Shadow
    private int x;
    @Shadow
    private int y;
    @Shadow
    private int windowedX;
    @Shadow
    private int windowedY;
    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow
    private int windowedWidth;
    @Shadow
    private int windowedHeight;
    @Shadow
    private int framebufferWidth;
    @Shadow
    private int framebufferHeight;
    @Shadow
    private boolean fullscreen;
    @Shadow
    @Final
    private WindowEventHandler eventHandler;
    @Shadow
    @Final
    private ScreenManager screenManager;
    @Shadow
    private Optional<VideoMode> preferredFullscreenVideoMode;
    @Shadow
    private boolean focused;
    @Unique
    private int defaultWidth;
    @Unique
    private int defaultHeight;
    @Unique
    private int realWidth;
    @Unique
    private int realHeight;
    @Unique
    private int realFramebufferWidth;
    @Unique
    private int realFramebufferHeight;

    @Shadow
    protected abstract void setMode();

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onInit(WindowEventHandler eventHandler, DisplayData displayData, String fullscreenVideoModeString, String title, GpuBackend backend, CallbackInfo ci) {
        this.defaultWidth = displayData.width();
        this.defaultHeight = displayData.height();
        this.realWidth = this.width;
        this.realHeight = this.height;
        this.realFramebufferWidth = this.framebufferWidth;
        this.realFramebufferHeight = this.framebufferHeight;
        this.windowedWidth = this.framebufferWidth = this.defaultWidth;
        this.width = this.framebufferWidth;
        this.windowedHeight = this.framebufferHeight = this.defaultHeight;
        this.height = this.framebufferHeight;
    }

    @Inject(method={"onFocus", "onEnter", "onIconify"}, at={@At(value="HEAD")}, cancellable=true)
    private void cancelEvents(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method={"onResize"}, at={@At(value="HEAD")}, cancellable=true)
    private void cancelResize(long window, int width, int height, CallbackInfo ci) {
        this.realWidth = width;
        this.realHeight = height;
        ci.cancel();
    }

    @Inject(method={"onFramebufferResize"}, at={@At(value="HEAD")}, cancellable=true)
    private void cancelFramebufferResize(long window, int width, int height, CallbackInfo ci) {
        this.realFramebufferWidth = width;
        this.realFramebufferHeight = height;
        ci.cancel();
    }

    @WrapMethod(method={"setMode"})
    private void wrapSetMode(Operation<Void> original) {
        int prevWidth = this.width;
        int prevHeight = this.height;
        int prevWindowedWidth = this.windowedWidth;
        int prevWindowedHeight = this.windowedHeight;
        original.call(new Object[0]);
        this.realWidth = this.width;
        this.realHeight = this.height;
        this.width = prevWidth;
        this.height = prevHeight;
        this.windowedWidth = prevWindowedWidth;
        this.windowedHeight = prevWindowedHeight;
    }

    @Inject(method={"setWindowed"}, at={@At(value="HEAD")}, cancellable=true)
    private void setWindowedSize(int width, int height, CallbackInfo ci) {
        this.fullscreen = false;
        this.fabric_resize(width, height);
        ci.cancel();
    }

    @Override
    public int fabric_getRealWidth() {
        return this.realWidth;
    }

    @Override
    public int fabric_getRealHeight() {
        return this.realHeight;
    }

    @Override
    public int fabric_getRealFramebufferWidth() {
        return this.realFramebufferWidth;
    }

    @Override
    public int fabric_getRealFramebufferHeight() {
        return this.realFramebufferHeight;
    }

    @Override
    public void fabric_resetSize() {
        this.fabric_resize(this.defaultWidth, this.defaultHeight);
    }

    @Override
    public void fabric_resize(int width, int height) {
        if (width == this.width && width == this.windowedWidth && width == this.framebufferWidth && height == this.height && height == this.windowedHeight && height == this.framebufferHeight) {
            return;
        }
        Monitor monitor = this.screenManager.findBestMonitor((Window)((Object)this));
        if (monitor != null) {
            VideoMode videoMode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
            this.x += (this.windowedWidth - width) / 2;
            this.y += (this.windowedHeight - height) / 2;
            if (this.x + width > monitor.getX() + videoMode.getWidth()) {
                this.x = monitor.getX() + videoMode.getWidth() - width;
            }
            if (this.x < monitor.getX()) {
                this.x = monitor.getX();
            }
            if (this.y + height > monitor.getY() + videoMode.getHeight()) {
                this.y = monitor.getY() + videoMode.getHeight() - height;
            }
            if (this.y < monitor.getY()) {
                this.y = monitor.getY();
            }
            this.windowedX = this.x;
            this.windowedY = this.y;
        }
        this.windowedWidth = this.framebufferWidth = width;
        this.width = this.framebufferWidth;
        this.windowedHeight = this.framebufferHeight = height;
        this.height = this.framebufferHeight;
        this.setMode();
        this.eventHandler.resizeGui();
    }

    @Override
    public void fabric_focus() {
        this.focused = true;
    }
}

