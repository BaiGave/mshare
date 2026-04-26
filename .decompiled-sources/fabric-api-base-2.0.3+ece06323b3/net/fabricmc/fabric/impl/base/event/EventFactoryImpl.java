/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.base.event;

import com.google.common.collect.MapMaker;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.ArrayBackedEvent;
import net.minecraft.resources.Identifier;

public final class EventFactoryImpl {
    private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private EventFactoryImpl() {
    }

    public static void invalidate() {
        ARRAY_BACKED_EVENTS.forEach(ArrayBackedEvent::update);
    }

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        ArrayBackedEvent<? super T> event = new ArrayBackedEvent<T>(type, invokerFactory);
        ARRAY_BACKED_EVENTS.add(event);
        return event;
    }

    public static void ensureContainsDefault(Identifier[] defaultPhases) {
        for (Identifier id : defaultPhases) {
            if (!id.equals(Event.DEFAULT_PHASE)) continue;
            return;
        }
        throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
    }

    public static void ensureNoDuplicates(Identifier[] defaultPhases) {
        for (int i = 0; i < defaultPhases.length; ++i) {
            for (int j = i + 1; j < defaultPhases.length; ++j) {
                if (!defaultPhases[i].equals(defaultPhases[j])) continue;
                throw new IllegalArgumentException("Duplicate event phase: " + String.valueOf(defaultPhases[i]));
            }
        }
    }

    private static <T> T buildEmptyInvoker(Class<T> handlerClass, Function<T[], T> invokerSetup) {
        Method funcIfMethod = null;
        for (Method m : handlerClass.getMethods()) {
            if ((m.getModifiers() & 0x802) != 0) continue;
            if (funcIfMethod != null) {
                throw new IllegalStateException("Multiple virtual methods in " + String.valueOf(handlerClass) + "; cannot build empty invoker!");
            }
            funcIfMethod = m;
        }
        if (funcIfMethod == null) {
            throw new IllegalStateException("No virtual methods in " + String.valueOf(handlerClass) + "; cannot build empty invoker!");
        }
        Object defValue = null;
        try {
            MethodHandle target = MethodHandles.lookup().unreflect(funcIfMethod);
            MethodType type = target.type().dropParameterTypes(0, 1);
            if (type.returnType() != Void.TYPE) {
                MethodType objTargetType = MethodType.genericMethodType(type.parameterCount()).changeReturnType((Class<?>)type.returnType()).insertParameterTypes(0, new Class[]{target.type().parameterType(0)});
                MethodHandle objTarget = MethodHandles.explicitCastArguments(target, objTargetType);
                Object[] args2 = new Object[target.type().parameterCount()];
                args2[0] = invokerSetup.apply((Object[][])((Object[])Array.newInstance(handlerClass, 0)));
                defValue = objTarget.invokeWithArguments(args2);
            }
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
        Object returnValue = defValue;
        return (T)Proxy.newProxyInstance(EventFactoryImpl.class.getClassLoader(), new Class[]{handlerClass}, (proxy, method, args) -> returnValue);
    }
}

