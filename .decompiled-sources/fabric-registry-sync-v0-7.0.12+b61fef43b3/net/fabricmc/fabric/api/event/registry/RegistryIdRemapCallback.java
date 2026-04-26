/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface RegistryIdRemapCallback<T> {
    public void onRemap(RemapState<T> var1);

    public static <T> Event<RegistryIdRemapCallback<T>> event(Registry<T> registry) {
        return ListenableRegistry.get(registry).fabric_getRemapEvent();
    }

    public static interface RemapState<T> {
        public Int2IntMap getRawIdChangeMap();

        public Identifier getIdFromOld(int var1);

        public Identifier getIdFromNew(int var1);
    }
}

