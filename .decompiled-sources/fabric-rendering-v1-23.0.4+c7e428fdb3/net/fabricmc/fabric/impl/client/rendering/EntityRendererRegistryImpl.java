/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.HashMap;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class EntityRendererRegistryImpl {
    private static HashMap<EntityType<?>, EntityRendererProvider<?>> map = new HashMap();
    private static BiConsumer<EntityType<?>, EntityRendererProvider<?>> handler = (type, function) -> map.put((EntityType<?>)type, (EntityRendererProvider<?>)function);

    public static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> factory) {
        handler.accept(entityType, factory);
    }

    public static void setup(BiConsumer<EntityType<?>, EntityRendererProvider<?>> vanillaHandler) {
        map.forEach(vanillaHandler);
        handler = vanillaHandler;
    }

    private EntityRendererRegistryImpl() {
    }
}

