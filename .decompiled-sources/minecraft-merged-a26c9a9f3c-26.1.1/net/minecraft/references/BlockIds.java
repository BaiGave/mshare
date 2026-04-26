/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class BlockIds {
    public static final ResourceKey<Block> PUMPKIN = BlockIds.createKey("pumpkin");
    public static final ResourceKey<Block> PUMPKIN_STEM = BlockIds.createKey("pumpkin_stem");
    public static final ResourceKey<Block> ATTACHED_PUMPKIN_STEM = BlockIds.createKey("attached_pumpkin_stem");
    public static final ResourceKey<Block> MELON = BlockIds.createKey("melon");
    public static final ResourceKey<Block> MELON_STEM = BlockIds.createKey("melon_stem");
    public static final ResourceKey<Block> ATTACHED_MELON_STEM = BlockIds.createKey("attached_melon_stem");
    public static final ResourceKey<Block> DIRT = BlockIds.createKey("dirt");

    private static ResourceKey<Block> createKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(name));
    }
}

