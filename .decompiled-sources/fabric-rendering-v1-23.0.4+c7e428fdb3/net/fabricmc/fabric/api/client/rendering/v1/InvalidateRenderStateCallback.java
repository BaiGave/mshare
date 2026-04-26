/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface InvalidateRenderStateCallback {
    public static final Event<InvalidateRenderStateCallback> EVENT = EventFactory.createArrayBacked(InvalidateRenderStateCallback.class, listeners -> () -> {
        for (InvalidateRenderStateCallback event : listeners) {
            event.onInvalidate();
        }
    });

    public void onInvalidate();
}

