/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantCache;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={Item.class})
public class ItemMixin
implements ItemVariantCache {
    @Unique
    private final ItemVariant cachedItemVariant = new ItemVariantImpl((Item)((Object)this), DataComponentPatch.EMPTY);

    @Override
    public ItemVariant fabric_getCachedItemVariant() {
        return this.cachedItemVariant;
    }
}

