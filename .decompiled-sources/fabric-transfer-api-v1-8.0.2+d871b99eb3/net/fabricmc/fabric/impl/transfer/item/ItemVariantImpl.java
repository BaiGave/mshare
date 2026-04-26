/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantCache;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class ItemVariantImpl
implements ItemVariant {
    private final Item item;
    private final DataComponentPatch components;
    private final int hashCode;
    private volatile @Nullable ItemStack cachedStack = null;

    public static ItemVariant of(Item item, DataComponentPatch components) {
        Objects.requireNonNull(item, "Item may not be null.");
        Objects.requireNonNull(components, "Components may not be null.");
        if (components.isEmpty() || item == Items.AIR) {
            return ((ItemVariantCache)((Object)item)).fabric_getCachedItemVariant();
        }
        return new ItemVariantImpl(item, components);
    }

    public static ItemVariant of(Holder<Item> item, DataComponentPatch components) {
        return ItemVariantImpl.of(item.value(), components);
    }

    public ItemVariantImpl(Item item, DataComponentPatch components) {
        this.item = item;
        this.components = components;
        this.hashCode = Objects.hash(item, components);
    }

    @Override
    public Item getObject() {
        return this.item;
    }

    @Override
    public @Nullable DataComponentPatch getComponentsPatch() {
        return this.components;
    }

    @Override
    public DataComponentMap getComponents() {
        return this.getCachedStack().getComponents();
    }

    @Override
    public ItemVariant withComponents(DataComponentPatch patch) {
        return ItemVariantImpl.of(this.item, TransferApiImpl.mergePatches(this.getComponentsPatch(), patch));
    }

    @Override
    public boolean isBlank() {
        return this.item == Items.AIR;
    }

    public String toString() {
        return "ItemVariant{item=" + String.valueOf(this.item) + ", components=" + String.valueOf(this.components) + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ItemVariantImpl ItemVariant2 = (ItemVariantImpl)o;
        return this.hashCode == ItemVariant2.hashCode && this.item == ItemVariant2.item && this.componentsMatch(ItemVariant2.components);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public ItemStack getCachedStack() {
        ItemStack ret = this.cachedStack;
        if (ret == null) {
            this.cachedStack = ret = this.toStack();
        }
        return ret;
    }

    public static int getMaxStackSize(ItemVariant variant) {
        return variant.getComponents().getOrDefault(DataComponents.MAX_STACK_SIZE, variant.getItem().getDefaultMaxStackSize());
    }
}

