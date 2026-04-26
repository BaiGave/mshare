/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import java.util.Objects;
import net.fabricmc.fabric.impl.content.registry.VillagerInteractionRegistriesImpl;
import net.fabricmc.fabric.mixin.content.registry.GiveGiftToHeroAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerInteractionRegistries {
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagerInteractionRegistries.class);

    private VillagerInteractionRegistries() {
    }

    @Deprecated
    public static void registerGatherableItem(ItemLike item) {
        Objects.requireNonNull(item.asItem(), "Item cannot be null!");
        VillagerInteractionRegistriesImpl.getGatherableItemRegistry().add(item.asItem());
    }

    public static void registerCompostable(ItemLike item) {
        Objects.requireNonNull(item.asItem(), "Item cannot be null!");
        VillagerInteractionRegistriesImpl.getCompostableRegistry().add(item.asItem());
    }

    public static void registerFood(ItemLike item, int foodValue) {
        Objects.requireNonNull(item.asItem(), "Item cannot be null!");
        Integer oldValue = VillagerInteractionRegistriesImpl.getFoodRegistry().put(item.asItem(), foodValue);
        if (oldValue != null) {
            LOGGER.info("Overriding previous food value of {}, was: {}, now: {}", item.asItem().toString(), oldValue, foodValue);
        }
    }

    public static void registerGiftLootTable(ResourceKey<VillagerProfession> profession, ResourceKey<LootTable> lootTable) {
        Objects.requireNonNull(profession, "Profession cannot be null!");
        Objects.requireNonNull(lootTable, "Loot table identifier cannot be null!");
        ResourceKey<LootTable> oldValue = GiveGiftToHeroAccessor.fabric_getGifts().put(profession, lootTable);
        if (oldValue != null) {
            LOGGER.info("Overriding previous gift loot table of {} profession, was: {}, now: {}", profession.identifier(), oldValue, lootTable);
        }
    }
}

