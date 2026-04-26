/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils;
import net.fabricmc.fabric.mixin.content.registry.VillagerAccessor;
import net.fabricmc.fabric.mixin.content.registry.WorkAtComposterAccessor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;

public final class VillagerInteractionRegistriesImpl {
    private static final Set<Item> GATHERABLE_ITEMS = new HashSet<Item>();

    private VillagerInteractionRegistriesImpl() {
    }

    public static Set<Item> getGatherableItemRegistry() {
        return GATHERABLE_ITEMS;
    }

    public static List<Item> getCompostableRegistry() {
        return ImmutableCollectionUtils.getAsMutableList(WorkAtComposterAccessor::fabric_getCompostable, WorkAtComposterAccessor::fabric_setCompostables);
    }

    public static Map<Item, Integer> getFoodRegistry() {
        return ImmutableCollectionUtils.getAsMutableMap(() -> Villager.FOOD_POINTS, VillagerAccessor::fabric_setItemFoodValues);
    }
}

