/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.debug.v1;

import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.debug.v1.DebugValueFactory;
import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.entity.Entity;

public final class EntityDebugSubscriptionRegistry {
    public static <T, E extends Entity> void register(DebugSubscription<T> debugSubscription, Predicate<Entity> shouldSubscribe, DebugValueFactory<E, T> valueFactory) {
        Objects.requireNonNull(debugSubscription);
        EntityDebugSubscriptionRegistryImpl.register(debugSubscription, shouldSubscribe, valueFactory);
    }

    public static <T, E extends Entity> void register(DebugSubscription<T> debugSubscription, Predicate<Entity> shouldSubscribe, DebugValueFactory<E, T> valueFactory, boolean isEnabledFlag) {
        if (isEnabledFlag) {
            EntityDebugSubscriptionRegistry.register(debugSubscription, shouldSubscribe, valueFactory);
        }
    }
}

