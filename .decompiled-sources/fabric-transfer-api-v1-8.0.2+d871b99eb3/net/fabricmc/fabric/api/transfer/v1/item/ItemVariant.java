/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.VariantCodecs;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ItemVariant
extends TransferVariant<Item> {
    public static final Codec<ItemVariant> CODEC = VariantCodecs.ITEM_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemVariant> PACKET_CODEC = VariantCodecs.ITEM_PACKET_CODEC;

    public static ItemVariant blank() {
        return ItemVariant.of(Items.AIR);
    }

    public static ItemVariant of(ItemStack stack) {
        return ItemVariant.of(stack.getItem(), stack.getComponentsPatch());
    }

    public static ItemVariant of(ItemLike item) {
        return ItemVariant.of(item, DataComponentPatch.EMPTY);
    }

    public static ItemVariant of(ItemLike item, DataComponentPatch components) {
        return ItemVariantImpl.of(item.asItem(), components);
    }

    default public boolean matches(ItemStack stack) {
        return this.isOf(stack.getItem()) && Objects.equals(stack.getComponentsPatch(), this.getComponentsPatch());
    }

    default public Item getItem() {
        return (Item)this.getObject();
    }

    @Override
    default public Holder<Item> typeHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    default public ItemStack toStack() {
        return this.toStack(1);
    }

    default public ItemStack toStack(int count) {
        if (this.isBlank()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(this.typeHolder(), count, this.getComponentsPatch());
    }

    public ItemVariant withComponents(DataComponentPatch var1);
}

