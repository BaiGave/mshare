/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.debug.v1.renderer;

import java.util.Objects;
import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererFactory;
import net.fabricmc.fabric.impl.debug.client.renderer.DebugRendererRegistryImpl;
import net.minecraft.util.debug.DebugSubscription;

public final class DebugRendererRegistry {
    public static <T> void register(DebugSubscription<T> debugSubscription, DebugRendererFactory rendererFactory) {
        Objects.requireNonNull(debugSubscription);
        DebugRendererRegistryImpl.register(debugSubscription, rendererFactory);
    }

    public static <T> void register(DebugSubscription<T> debugSubscription, DebugRendererFactory rendererFactory, boolean isEnabledFlag) {
        if (isEnabledFlag) {
            DebugRendererRegistry.register(debugSubscription, rendererFactory);
        }
    }
}

