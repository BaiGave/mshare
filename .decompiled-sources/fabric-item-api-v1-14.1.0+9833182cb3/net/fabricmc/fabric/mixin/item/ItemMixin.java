/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Item.class})
abstract class ItemMixin
implements ItemExtensions,
FabricItem {
    @Unique
    private @Nullable EquipmentSlotProvider equipmentSlotProvider;
    @Unique
    private @Nullable CustomDamageHandler customDamageHandler;

    ItemMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onConstruct(Item.Properties properties, CallbackInfo info) {
        FabricItemInternals.onBuild(properties, (Item)((Object)this));
    }

    @Override
    public @Nullable EquipmentSlotProvider fabric_getEquipmentSlotProvider() {
        return this.equipmentSlotProvider;
    }

    @Override
    public void fabric_setEquipmentSlotProvider(@Nullable EquipmentSlotProvider equipmentSlotProvider) {
        this.equipmentSlotProvider = equipmentSlotProvider;
    }

    @Override
    public @Nullable CustomDamageHandler fabric_getCustomDamageHandler() {
        return this.customDamageHandler;
    }

    @Override
    public void fabric_setCustomDamageHandler(@Nullable CustomDamageHandler handler) {
        this.customDamageHandler = handler;
    }
}

