/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.object.builder.v1.entity.MinecartComparatorLogic;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecartComparatorLogicRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecartComparatorLogicRegistry.class);
    private static final Map<EntityType<?>, MinecartComparatorLogic<?>> LOGICS = new IdentityHashMap();

    private MinecartComparatorLogicRegistry() {
    }

    public static @Nullable MinecartComparatorLogic<AbstractMinecart> getCustomComparatorLogic(EntityType<?> type) {
        return LOGICS.get(type);
    }

    public static <T extends AbstractMinecart> void register(EntityType<T> type, MinecartComparatorLogic<? super T> logic) {
        Objects.requireNonNull(type, "Entity type cannot be null");
        Objects.requireNonNull(logic, "Logic cannot be null");
        if (LOGICS.put(type, logic) != null) {
            LOGGER.warn("Overriding existing minecart comparator logic for entity type {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
    }
}

