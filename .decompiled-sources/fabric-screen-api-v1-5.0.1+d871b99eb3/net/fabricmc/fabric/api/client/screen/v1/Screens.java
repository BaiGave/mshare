/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.screen.v1;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.fabricmc.fabric.mixin.screen.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public final class Screens {
    public static List<AbstractWidget> getWidgets(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getButtons();
    }

    @Deprecated
    public static Font getFont(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return screen.getFont();
    }

    public static Minecraft getMinecraft(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ((ScreenAccessor)((Object)screen)).getClient();
    }

    private Screens() {
    }
}

