/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemIds {
    public static final ResourceKey<Item> PUMPKIN_SEEDS = ItemIds.createKey("pumpkin_seeds");
    public static final ResourceKey<Item> MELON_SEEDS = ItemIds.createKey("melon_seeds");

    private static ResourceKey<Item> createKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace(name));
    }
}

