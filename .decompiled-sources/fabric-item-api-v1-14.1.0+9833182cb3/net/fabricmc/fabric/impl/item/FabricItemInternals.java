/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import java.util.WeakHashMap;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public final class FabricItemInternals {
    private static final WeakHashMap<Item.Properties, ExtraData> extraData = new WeakHashMap();

    private FabricItemInternals() {
    }

    public static ExtraData computeExtraData(Item.Properties properties) {
        return extraData.computeIfAbsent(properties, s -> new ExtraData());
    }

    public static void onBuild(Item.Properties properties, Item item) {
        ExtraData data = extraData.get(properties);
        if (data != null) {
            ((ItemExtensions)((Object)item)).fabric_setEquipmentSlotProvider(data.equipmentSlotProvider);
            ((ItemExtensions)((Object)item)).fabric_setCustomDamageHandler(data.customDamageHandler);
        }
    }

    public static final class ExtraData {
        private @Nullable EquipmentSlotProvider equipmentSlotProvider;
        private @Nullable CustomDamageHandler customDamageHandler;

        public void equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
            this.equipmentSlotProvider = equipmentSlotProvider;
        }

        public void customDamage(CustomDamageHandler handler) {
            this.customDamageHandler = handler;
        }
    }
}

