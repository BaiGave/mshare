/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import org.jspecify.annotations.Nullable;

public interface ItemExtensions {
    public @Nullable EquipmentSlotProvider fabric_getEquipmentSlotProvider();

    public void fabric_setEquipmentSlotProvider(EquipmentSlotProvider var1);

    public @Nullable CustomDamageHandler fabric_getCustomDamageHandler();

    public void fabric_setCustomDamageHandler(CustomDamageHandler var1);
}

