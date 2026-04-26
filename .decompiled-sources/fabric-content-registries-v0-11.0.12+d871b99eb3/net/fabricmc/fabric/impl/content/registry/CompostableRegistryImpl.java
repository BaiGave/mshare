/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import net.fabricmc.fabric.api.registry.CompostableRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public class CompostableRegistryImpl
implements CompostableRegistry {
    @Override
    public Float get(ItemLike item) {
        return Float.valueOf(ComposterBlock.COMPOSTABLES.getOrDefault((Object)item.asItem(), 0.0f));
    }

    @Override
    public void add(ItemLike item, Float chance) {
        ComposterBlock.COMPOSTABLES.put((ItemLike)item.asItem(), chance);
    }

    @Override
    public void add(TagKey<Item> tag, Float chance) {
        throw new UnsupportedOperationException("Tags currently not supported!");
    }

    @Override
    public void remove(ItemLike item) {
        ComposterBlock.COMPOSTABLES.removeFloat(item.asItem());
    }

    @Override
    public void remove(TagKey<Item> tag) {
        throw new UnsupportedOperationException("Tags currently not supported!");
    }

    @Override
    public void clear(ItemLike item) {
        throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
    }

    @Override
    public void clear(TagKey<Item> tag) {
        throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
    }
}

