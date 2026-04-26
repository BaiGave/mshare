/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.screen.ButtonList;
import net.fabricmc.fabric.impl.client.screen.ScreenEventFactory;
import net.fabricmc.fabric.impl.client.screen.ScreenExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Screen.class})
abstract class ScreenMixin
implements ScreenExtensions {
    @Shadow
    @Final
    private List<NarratableEntry> narratables;
    @Shadow
    @Final
    private List<GuiEventListener> children;
    @Shadow
    @Final
    private List<Renderable> renderables;
    @Unique
    private ButtonList fabricButtons;
    @Unique
    private Event<ScreenEvents.Remove> removeEvent;
    @Unique
    private Event<ScreenEvents.BeforeTick> beforeTickEvent;
    @Unique
    private Event<ScreenEvents.AfterTick> afterTickEvent;
    @Unique
    private Event<ScreenEvents.BeforeExtract> beforeRenderEvent;
    @Unique
    private Event<ScreenEvents.AfterBackground> afterBackgroundEvent;
    @Unique
    private Event<ScreenEvents.AfterExtract> afterRenderEvent;
    @Unique
    private Event<ScreenKeyboardEvents.AllowKeyPress> allowKeyPressEvent;
    @Unique
    private Event<ScreenKeyboardEvents.BeforeKeyPress> beforeKeyPressEvent;
    @Unique
    private Event<ScreenKeyboardEvents.AfterKeyPress> afterKeyPressEvent;
    @Unique
    private Event<ScreenKeyboardEvents.AllowKeyRelease> allowKeyReleaseEvent;
    @Unique
    private Event<ScreenKeyboardEvents.BeforeKeyRelease> beforeKeyReleaseEvent;
    @Unique
    private Event<ScreenKeyboardEvents.AfterKeyRelease> afterKeyReleaseEvent;
    @Unique
    private Event<ScreenMouseEvents.AllowMouseClick> allowMouseClickEvent;
    @Unique
    private Event<ScreenMouseEvents.BeforeMouseClick> beforeMouseClickEvent;
    @Unique
    private Event<ScreenMouseEvents.AfterMouseClick> afterMouseClickEvent;
    @Unique
    private Event<ScreenMouseEvents.AllowMouseRelease> allowMouseReleaseEvent;
    @Unique
    private Event<ScreenMouseEvents.BeforeMouseRelease> beforeMouseReleaseEvent;
    @Unique
    private Event<ScreenMouseEvents.AfterMouseRelease> afterMouseReleaseEvent;
    @Unique
    private Event<ScreenMouseEvents.AllowMouseDrag> allowMouseDragEvent;
    @Unique
    private Event<ScreenMouseEvents.BeforeMouseDrag> beforeMouseDragEvent;
    @Unique
    private Event<ScreenMouseEvents.AfterMouseDrag> afterMouseDragEvent;
    @Unique
    private Event<ScreenMouseEvents.AllowMouseScroll> allowMouseScrollEvent;
    @Unique
    private Event<ScreenMouseEvents.BeforeMouseScroll> beforeMouseScrollEvent;
    @Unique
    private Event<ScreenMouseEvents.AfterMouseScroll> afterMouseScrollEvent;

    ScreenMixin() {
    }

    @Inject(method={"extractRenderStateWithTooltipAndSubtitles"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", shift=At.Shift.AFTER)})
    public final void extractWithTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        ScreenEvents.afterBackground((Screen)((Object)this)).invoker().afterBackground((Screen)((Object)this), graphics, mouseX, mouseY, deltaTicks);
    }

    @Inject(method={"init(II)V"}, at={@At(value="HEAD")})
    private void beforeInitScreen(int width, int height, CallbackInfo ci) {
        this.beforeInit(width, height);
    }

    @Inject(method={"init(II)V"}, at={@At(value="TAIL")})
    private void afterInitScreen(int width, int height, CallbackInfo ci) {
        this.afterInit(width, height);
    }

    @Inject(method={"resize"}, at={@At(value="HEAD")})
    private void beforeResizeScreen(int width, int height, CallbackInfo ci) {
        this.beforeInit(width, height);
    }

    @Inject(method={"resize"}, at={@At(value="TAIL")})
    private void afterResizeScreen(int width, int height, CallbackInfo ci) {
        this.afterInit(width, height);
    }

    @Unique
    private void beforeInit(int width, int height) {
        this.fabricButtons = null;
        this.removeEvent = ScreenEventFactory.createRemoveEvent();
        this.beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
        this.afterBackgroundEvent = ScreenEventFactory.createAfterBackgroundEvent();
        this.afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
        this.beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
        this.afterTickEvent = ScreenEventFactory.createAfterTickEvent();
        this.allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
        this.beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
        this.afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
        this.allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
        this.beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
        this.afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();
        this.allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
        this.beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
        this.afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
        this.allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
        this.beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
        this.afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
        this.allowMouseDragEvent = ScreenEventFactory.createAllowMouseDragEvent();
        this.beforeMouseDragEvent = ScreenEventFactory.createBeforeMouseDragEvent();
        this.afterMouseDragEvent = ScreenEventFactory.createAfterMouseDragEvent();
        this.allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
        this.beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
        this.afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();
        ScreenEvents.BEFORE_INIT.invoker().beforeInit(Minecraft.getInstance(), (Screen)((Object)this), width, height);
    }

    @Unique
    private void afterInit(int width, int height) {
        ScreenEvents.AFTER_INIT.invoker().afterInit(Minecraft.getInstance(), (Screen)((Object)this), width, height);
    }

    @Override
    public List<AbstractWidget> fabric_getButtons() {
        if (this.fabricButtons == null) {
            this.fabricButtons = new ButtonList(this.renderables, this.narratables, this.children);
        }
        return this.fabricButtons;
    }

    @Unique
    private <T> Event<T> ensureEventsAreInitialized(Event<T> event) {
        if (event == null) {
            throw new IllegalStateException(String.format("[fabric-screen-api-v1] The current screen (%s) has not been correctly initialised, please send this crash log to the mod author. This is usually caused by calling setScreen on the wrong thread.", this.getClass().getName()));
        }
        return event;
    }

    @Override
    public Event<ScreenEvents.Remove> fabric_getRemoveEvent() {
        return this.ensureEventsAreInitialized(this.removeEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeTick> fabric_getBeforeTickEvent() {
        return this.ensureEventsAreInitialized(this.beforeTickEvent);
    }

    @Override
    public Event<ScreenEvents.AfterTick> fabric_getAfterTickEvent() {
        return this.ensureEventsAreInitialized(this.afterTickEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeExtract> fabric_getBeforeRenderEvent() {
        return this.ensureEventsAreInitialized(this.beforeRenderEvent);
    }

    @Override
    public Event<ScreenEvents.AfterBackground> fabric_getAfterBackgroundEvent() {
        return this.ensureEventsAreInitialized(this.afterBackgroundEvent);
    }

    @Override
    public Event<ScreenEvents.AfterExtract> fabric_getAfterRenderEvent() {
        return this.ensureEventsAreInitialized(this.afterRenderEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.AllowKeyPress> fabric_getAllowKeyPressEvent() {
        return this.ensureEventsAreInitialized(this.allowKeyPressEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.BeforeKeyPress> fabric_getBeforeKeyPressEvent() {
        return this.ensureEventsAreInitialized(this.beforeKeyPressEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.AfterKeyPress> fabric_getAfterKeyPressEvent() {
        return this.ensureEventsAreInitialized(this.afterKeyPressEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.AllowKeyRelease> fabric_getAllowKeyReleaseEvent() {
        return this.ensureEventsAreInitialized(this.allowKeyReleaseEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.BeforeKeyRelease> fabric_getBeforeKeyReleaseEvent() {
        return this.ensureEventsAreInitialized(this.beforeKeyReleaseEvent);
    }

    @Override
    public Event<ScreenKeyboardEvents.AfterKeyRelease> fabric_getAfterKeyReleaseEvent() {
        return this.ensureEventsAreInitialized(this.afterKeyReleaseEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AllowMouseClick> fabric_getAllowMouseClickEvent() {
        return this.ensureEventsAreInitialized(this.allowMouseClickEvent);
    }

    @Override
    public Event<ScreenMouseEvents.BeforeMouseClick> fabric_getBeforeMouseClickEvent() {
        return this.ensureEventsAreInitialized(this.beforeMouseClickEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AfterMouseClick> fabric_getAfterMouseClickEvent() {
        return this.ensureEventsAreInitialized(this.afterMouseClickEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AllowMouseRelease> fabric_getAllowMouseReleaseEvent() {
        return this.ensureEventsAreInitialized(this.allowMouseReleaseEvent);
    }

    @Override
    public Event<ScreenMouseEvents.BeforeMouseRelease> fabric_getBeforeMouseReleaseEvent() {
        return this.ensureEventsAreInitialized(this.beforeMouseReleaseEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AfterMouseRelease> fabric_getAfterMouseReleaseEvent() {
        return this.ensureEventsAreInitialized(this.afterMouseReleaseEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AllowMouseDrag> fabric_getAllowMouseDragEvent() {
        return this.ensureEventsAreInitialized(this.allowMouseDragEvent);
    }

    @Override
    public Event<ScreenMouseEvents.BeforeMouseDrag> fabric_getBeforeMouseDragEvent() {
        return this.ensureEventsAreInitialized(this.beforeMouseDragEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AfterMouseDrag> fabric_getAfterMouseDragEvent() {
        return this.ensureEventsAreInitialized(this.afterMouseDragEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AllowMouseScroll> fabric_getAllowMouseScrollEvent() {
        return this.ensureEventsAreInitialized(this.allowMouseScrollEvent);
    }

    @Override
    public Event<ScreenMouseEvents.BeforeMouseScroll> fabric_getBeforeMouseScrollEvent() {
        return this.ensureEventsAreInitialized(this.beforeMouseScrollEvent);
    }

    @Override
    public Event<ScreenMouseEvents.AfterMouseScroll> fabric_getAfterMouseScrollEvent() {
        return this.ensureEventsAreInitialized(this.afterMouseScrollEvent);
    }
}

