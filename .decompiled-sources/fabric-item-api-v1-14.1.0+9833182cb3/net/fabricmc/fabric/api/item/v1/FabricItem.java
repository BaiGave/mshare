/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.impl.item.FabricItemInternals;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

public interface FabricItem {
    default public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    default public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    default public @Nullable ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return ((Item)this).getCraftingRemainder();
    }

    default public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
        return context == EnchantingContext.PRIMARY ? enchantment.value().isPrimaryItem(stack) : enchantment.value().canEnchant(stack);
    }

    default public String getCreatorNamespace(ItemStack stack) {
        Set<Holder<Enchantment>> enchantments;
        Holder<Object> holder = stack.typeHolder();
        if ((this instanceof PotionItem || this instanceof TippedArrowItem) && stack.has(DataComponents.POTION_CONTENTS)) {
            Optional<Holder<Potion>> potion = stack.get(DataComponents.POTION_CONTENTS).potion();
            if (potion.isPresent()) {
                holder = potion.get();
            }
        } else if (stack.is(Items.ENCHANTED_BOOK) && stack.has(DataComponents.STORED_ENCHANTMENTS) && (enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS).keySet()).size() == 1) {
            holder = enchantments.iterator().next();
        }
        return holder.unwrapKey().orElseThrow().identifier().getNamespace();
    }

    public static interface Properties {
        default public Item.Properties equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
            FabricItemInternals.computeExtraData((Item.Properties)this).equipmentSlot(equipmentSlotProvider);
            return (Item.Properties)this;
        }

        default public Item.Properties customDamage(CustomDamageHandler handler) {
            FabricItemInternals.computeExtraData((Item.Properties)this).customDamage(handler);
            return (Item.Properties)this;
        }

        default public Item.Properties modelId(Identifier modelId) {
            return (Item.Properties)this;
        }

        default public @Nullable ResourceKey<Item> itemId() {
            throw new AssertionError((Object)"Implemented in Mixin");
        }
    }
}

