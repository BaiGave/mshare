/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.base.event;

import java.lang.reflect.Array;
import java.util.Arrays;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.minecraft.resources.Identifier;

class EventPhaseData<T>
extends SortableNode<EventPhaseData<T>> {
    final Identifier id;
    T[] listeners;

    EventPhaseData(Identifier id, Class<?> listenerClass) {
        this.id = id;
        this.listeners = (Object[])Array.newInstance(listenerClass, 0);
    }

    void addListener(T listener) {
        int oldLength = this.listeners.length;
        this.listeners = Arrays.copyOf(this.listeners, oldLength + 1);
        this.listeners[oldLength] = listener;
    }

    @Override
    protected String getDescription() {
        return this.id.toString();
    }
}

