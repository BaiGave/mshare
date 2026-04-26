/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ServerEntityEvents {
    public static final Event<Load> ENTITY_LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (entity, level) -> {
        for (Load callback : callbacks) {
            callback.onLoad(entity, level);
        }
    });
    public static final Event<Unload> ENTITY_UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (entity, level) -> {
        for (Unload callback : callbacks) {
            callback.onUnload(entity, level);
        }
    });
    public static final Event<EquipmentChange> EQUIPMENT_CHANGE = EventFactory.createArrayBacked(EquipmentChange.class, callbacks -> (livingEntity, equipmentSlot, previous, next) -> {
        for (EquipmentChange callback : callbacks) {
            callback.onChange(livingEntity, equipmentSlot, previous, next);
        }
    });

    private ServerEntityEvents() {
    }

    @FunctionalInterface
    public static interface EquipmentChange {
        public void onChange(LivingEntity var1, EquipmentSlot var2, ItemStack var3, ItemStack var4);
    }

    @FunctionalInterface
    public static interface Unload {
        public void onUnload(Entity var1, ServerLevel var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onLoad(Entity var1, ServerLevel var2);
    }
}

