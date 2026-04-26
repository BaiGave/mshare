/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class LivingEntityFeatureRenderEvents {
    public static final Event<AllowCapeRender> ALLOW_CAPE_RENDER = EventFactory.createArrayBacked(AllowCapeRender.class, listeners -> state -> {
        for (AllowCapeRender listener : listeners) {
            if (listener.allowCapeRender(state)) continue;
            return false;
        }
        return true;
    });

    private LivingEntityFeatureRenderEvents() {
    }

    @FunctionalInterface
    public static interface AllowCapeRender {
        public boolean allowCapeRender(AvatarRenderState var1);
    }
}

