/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenEvents {
    public static final Event<BeforeInit> BEFORE_INIT = EventFactory.createArrayBacked(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (BeforeInit callback : callbacks) {
            callback.beforeInit(client, screen, scaledWidth, scaledHeight);
        }
    });
    public static final Event<AfterInit> AFTER_INIT = EventFactory.createArrayBacked(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
        for (AfterInit callback : callbacks) {
            callback.afterInit(client, screen, scaledWidth, scaledHeight);
        }
    });

    public static Event<Remove> remove(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getRemoveEvent();
    }

    public static Event<BeforeExtract> beforeExtract(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeRenderEvent();
    }

    public static Event<AfterBackground> afterBackground(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterBackgroundEvent();
    }

    public static Event<AfterExtract> afterExtract(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterRenderEvent();
    }

    public static Event<BeforeTick> beforeTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getBeforeTickEvent();
    }

    public static Event<AfterTick> afterTick(Screen screen) {
        Objects.requireNonNull(screen, "Screen cannot be null");
        return ScreenExtensions.getExtensions(screen).fabric_getAfterTickEvent();
    }

    private ScreenEvents() {
    }

    @FunctionalInterface
    public static interface AfterInit {
        public void afterInit(Minecraft var1, Screen var2, int var3, int var4);
    }

    @FunctionalInterface
    public static interface BeforeInit {
        public void beforeInit(Minecraft var1, Screen var2, int var3, int var4);
    }

    @FunctionalInterface
    public static interface AfterTick {
        public void afterTick(Screen var1);
    }

    @FunctionalInterface
    public static interface BeforeTick {
        public void beforeTick(Screen var1);
    }

    @FunctionalInterface
    public static interface AfterExtract {
        public void afterExtract(Screen var1, GuiGraphicsExtractor var2, int var3, int var4, float var5);
    }

    @FunctionalInterface
    public static interface AfterBackground {
        public void afterBackground(Screen var1, GuiGraphicsExtractor var2, int var3, int var4, float var5);
    }

    @FunctionalInterface
    public static interface BeforeExtract {
        public void beforeExtract(Screen var1, GuiGraphicsExtractor var2, int var3, int var4, float var5);
    }

    @FunctionalInterface
    public static interface Remove {
        public void onRemove(Screen var1);
    }
}

