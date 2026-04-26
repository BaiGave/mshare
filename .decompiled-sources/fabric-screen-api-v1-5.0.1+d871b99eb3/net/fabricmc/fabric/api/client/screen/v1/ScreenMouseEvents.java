/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

public final class ScreenMouseEvents {
    public static Event<AllowMouseClick> allowMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseClickEvent();
    }

    public static Event<BeforeMouseClick> beforeMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseClickEvent();
    }

    public static Event<AfterMouseClick> afterMouseClick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseClickEvent();
    }

    public static Event<AllowMouseRelease> allowMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseReleaseEvent();
    }

    public static Event<BeforeMouseRelease> beforeMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseReleaseEvent();
    }

    public static Event<AfterMouseRelease> afterMouseRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseReleaseEvent();
    }

    public static Event<AllowMouseDrag> allowMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseDragEvent();
    }

    public static Event<BeforeMouseDrag> beforeMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseDragEvent();
    }

    public static Event<AfterMouseDrag> afterMouseDrag(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseDragEvent();
    }

    public static Event<AllowMouseScroll> allowMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowMouseScrollEvent();
    }

    public static Event<BeforeMouseScroll> beforeMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeMouseScrollEvent();
    }

    public static Event<AfterMouseScroll> afterMouseScroll(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterMouseScrollEvent();
    }

    private ScreenMouseEvents() {
    }

    @FunctionalInterface
    public static interface AfterMouseScroll {
        public boolean afterMouseScroll(Screen var1, double var2, double var4, double var6, double var8, boolean var10);
    }

    @FunctionalInterface
    public static interface BeforeMouseScroll {
        public void beforeMouseScroll(Screen var1, double var2, double var4, double var6, double var8);
    }

    @FunctionalInterface
    public static interface AllowMouseScroll {
        public boolean allowMouseScroll(Screen var1, double var2, double var4, double var6, double var8);
    }

    @FunctionalInterface
    public static interface AfterMouseDrag {
        public boolean afterMouseDrag(Screen var1, MouseButtonEvent var2, double var3, double var5, boolean var7);
    }

    @FunctionalInterface
    public static interface BeforeMouseDrag {
        public void beforeMouseDrag(Screen var1, MouseButtonEvent var2, double var3, double var5);
    }

    @FunctionalInterface
    public static interface AllowMouseDrag {
        public boolean allowMouseDrag(Screen var1, MouseButtonEvent var2, double var3, double var5);
    }

    @FunctionalInterface
    public static interface AfterMouseRelease {
        public boolean afterMouseRelease(Screen var1, MouseButtonEvent var2, boolean var3);
    }

    @FunctionalInterface
    public static interface BeforeMouseRelease {
        public void beforeMouseRelease(Screen var1, MouseButtonEvent var2);
    }

    @FunctionalInterface
    public static interface AllowMouseRelease {
        public boolean allowMouseRelease(Screen var1, MouseButtonEvent var2);
    }

    @FunctionalInterface
    public static interface AfterMouseClick {
        public boolean afterMouseClick(Screen var1, MouseButtonEvent var2, boolean var3);
    }

    @FunctionalInterface
    public static interface BeforeMouseClick {
        public void beforeMouseClick(Screen var1, MouseButtonEvent var2);
    }

    @FunctionalInterface
    public static interface AllowMouseClick {
        public boolean allowMouseClick(Screen var1, MouseButtonEvent var2);
    }
}

