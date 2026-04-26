/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.debug.v1;

import java.util.Objects;
import net.fabricmc.fabric.impl.debug.client.ClientDebugSubscriptionRegistryImpl;
import net.minecraft.util.debug.DebugSubscription;

public final class ClientDebugSubscriptionRegistry {
    public static <T> void register(DebugSubscription<T> debugSubscription) {
        Objects.requireNonNull(debugSubscription);
        ClientDebugSubscriptionRegistryImpl.register(debugSubscription);
    }

    public static <T> void register(DebugSubscription<T> debugSubscription, boolean isEnabledFlag) {
        if (isEnabledFlag) {
            ClientDebugSubscriptionRegistry.register(debugSubscription);
        }
    }
}

