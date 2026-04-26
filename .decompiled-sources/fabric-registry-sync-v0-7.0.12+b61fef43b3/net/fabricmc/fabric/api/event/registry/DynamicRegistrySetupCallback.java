/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;

@FunctionalInterface
public interface DynamicRegistrySetupCallback {
    public static final Event<DynamicRegistrySetupCallback> EVENT = EventFactory.createArrayBacked(DynamicRegistrySetupCallback.class, callbacks -> registryView -> {
        for (DynamicRegistrySetupCallback callback : callbacks) {
            callback.onRegistrySetup(registryView);
        }
    });

    public void onRegistrySetup(DynamicRegistryView var1);
}

