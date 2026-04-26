/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event;

import java.util.function.Function;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;
import net.minecraft.resources.Identifier;

public final class EventFactory {
    private EventFactory() {
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        return EventFactoryImpl.createArrayBacked(type, invokerFactory);
    }

    public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
        return EventFactory.createArrayBacked(type, listeners -> {
            if (((Object[])listeners).length == 0) {
                return emptyInvoker;
            }
            if (((Object[])listeners).length == 1) {
                return listeners[0];
            }
            return invokerFactory.apply((T[])listeners);
        });
    }

    public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, Identifier ... defaultPhases) {
        EventFactoryImpl.ensureContainsDefault(defaultPhases);
        EventFactoryImpl.ensureNoDuplicates(defaultPhases);
        Event<T> event = EventFactory.createArrayBacked(type, invokerFactory);
        for (int i = 1; i < defaultPhases.length; ++i) {
            event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
        }
        return event;
    }

    @Deprecated
    public static String getHandlerName(Object handler) {
        return handler.getClass().getName();
    }

    @Deprecated
    public static boolean isProfilingEnabled() {
        return false;
    }

    @Deprecated(forRemoval=true)
    public static void invalidate() {
        EventFactoryImpl.invalidate();
    }
}

