/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.fabricmc.fabric.mixin.object.builder.DefaultAttributesAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricDefaultAttributeRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricDefaultAttributeRegistry.class);

    private FabricDefaultAttributeRegistry() {
    }

    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder) {
        FabricDefaultAttributeRegistry.register(type, builder.build());
    }

    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier container) {
        if (DefaultAttributesAccessor.getRegistry().put(type, container) != null) {
            LOGGER.debug("Overriding existing registration for entity type {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
    }
}

