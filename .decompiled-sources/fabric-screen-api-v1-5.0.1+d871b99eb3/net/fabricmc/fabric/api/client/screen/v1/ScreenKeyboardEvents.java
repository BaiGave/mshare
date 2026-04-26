/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

public final class ScreenKeyboardEvents {
    public static Event<AllowKeyPress> allowKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyPressEvent();
    }

    public static Event<BeforeKeyPress> beforeKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyPressEvent();
    }

    public static Event<AfterKeyPress> afterKeyPress(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyPressEvent();
    }

    public static Event<AllowKeyRelease> allowKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAllowKeyReleaseEvent();
    }

    public static Event<BeforeKeyRelease> beforeKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeKeyReleaseEvent();
    }

    public static Event<AfterKeyRelease> afterKeyRelease(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterKeyReleaseEvent();
    }

    private ScreenKeyboardEvents() {
    }

    @FunctionalInterface
    public static interface AfterKeyRelease {
        public void afterKeyRelease(Screen var1, KeyEvent var2);
    }

    @FunctionalInterface
    public static interface BeforeKeyRelease {
        public void beforeKeyRelease(Screen var1, KeyEvent var2);
    }

    @FunctionalInterface
    public static interface AllowKeyRelease {
        public boolean allowKeyRelease(Screen var1, KeyEvent var2);
    }

    @FunctionalInterface
    public static interface AfterKeyPress {
        public void afterKeyPress(Screen var1, KeyEvent var2);
    }

    @FunctionalInterface
    public static interface BeforeKeyPress {
        public void beforeKeyPress(Screen var1, KeyEvent var2);
    }

    @FunctionalInterface
    public static interface AllowKeyPress {
        public boolean allowKeyPress(Screen var1, KeyEvent var2);
    }
}

