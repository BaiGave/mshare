/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.debug;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.debug.v1.DebugValueFactory;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;

public final class EntityDebugSubscriptionRegistryImpl {
    public static final Set<Entry> ENTITY_DEBUG_SUBSCRIPTIONS = new HashSet<Entry>();

    public static <T, E extends Entity> void register(DebugSubscription<T> debugSubscription, Predicate<Entity> shouldSubscribe, DebugValueFactory<E, T> valueFactory) {
        ENTITY_DEBUG_SUBSCRIPTIONS.add(new Entry(debugSubscription, shouldSubscribe, valueFactory));
    }

    public static void addDebugValues(Object entity, DebugValueSource.Registration registration) {
        for (Entry entry : ENTITY_DEBUG_SUBSCRIPTIONS) {
            if (!entry.shouldSubscribe().test((Entity)entity)) continue;
            registration.register(entry.debugSubscription(), () -> entry.valueFactory().create((Entity)entity));
        }
    }

    public record Entry(DebugSubscription<Object> debugSubscription, Predicate<Entity> shouldSubscribe, DebugValueFactory<Entity, ?> valueFactory) {
        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry)o;
            return Objects.equals(this.debugSubscription, entry.debugSubscription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.debugSubscription);
        }
    }
}

