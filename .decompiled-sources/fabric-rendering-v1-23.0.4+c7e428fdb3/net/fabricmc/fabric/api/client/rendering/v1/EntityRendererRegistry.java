/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Deprecated
public final class EntityRendererRegistry {
    public static <E extends Entity> void register(EntityType<? extends E> entityType, EntityRendererProvider<E> entityRendererProvider) {
        EntityRendererRegistryImpl.register(entityType, entityRendererProvider);
    }

    private EntityRendererRegistry() {
    }
}

