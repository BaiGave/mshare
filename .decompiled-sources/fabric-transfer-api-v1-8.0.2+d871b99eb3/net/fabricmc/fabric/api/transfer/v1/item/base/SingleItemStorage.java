/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item.base;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SingleItemStorage
extends SingleVariantStorage<ItemVariant> {
    @Override
    protected final ItemVariant getBlankVariant() {
        return ItemVariant.blank();
    }

    public void readValue(ValueInput data) {
        SingleVariantStorage.readValue(this, ItemVariant.CODEC, ItemVariant::blank, data);
    }

    public void writeValue(ValueOutput data) {
        SingleVariantStorage.writeValue(this, ItemVariant.CODEC, data);
    }
}

