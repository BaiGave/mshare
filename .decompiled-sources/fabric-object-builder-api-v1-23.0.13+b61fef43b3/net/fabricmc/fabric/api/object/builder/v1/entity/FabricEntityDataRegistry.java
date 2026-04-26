/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.fabricmc.fabric.impl.object.builder.FabricEntityDataRegistryImpl;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class FabricEntityDataRegistry {
    private FabricEntityDataRegistry() {
    }

    public static void register(Identifier id, EntityDataSerializer<?> handler) {
        FabricEntityDataRegistryImpl.register(id, handler);
    }

    public static @Nullable EntityDataSerializer<?> get(Identifier id) {
        return FabricEntityDataRegistryImpl.get(id);
    }

    public static @Nullable Identifier getId(EntityDataSerializer<?> handler) {
        return FabricEntityDataRegistryImpl.getId(handler);
    }
}

