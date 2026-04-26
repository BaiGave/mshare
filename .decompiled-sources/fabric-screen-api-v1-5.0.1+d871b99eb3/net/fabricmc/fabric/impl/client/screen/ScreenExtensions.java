/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.screen;

import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public interface ScreenExtensions {
    public static ScreenExtensions getExtensions(Screen screen) {
        return (ScreenExtensions)((Object)screen);
    }

    public List<AbstractWidget> fabric_getButtons();

    public Event<ScreenEvents.Remove> fabric_getRemoveEvent();

    public Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent();

    public Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent();

    public Event<ScreenEvents.BeforeExtract> fabric_getBeforeRenderEvent();

    public Event<ScreenEvents.AfterBackground> fabric_getAfterBackgroundEvent();

    public Event<ScreenEvents.AfterExtract> fabric_getAfterRenderEvent();

    public Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent();

    public Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent();

    public Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent();

    public Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent();

    public Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent();

    public Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent();

    public Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent();

    public Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent();

    public Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent();

    public Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent();

    public Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent();

    public Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent();

    public Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent();

    public Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent();

    public Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent();

    public Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent();

    public Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent();

    public Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent();
}

