/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Item2ObjectMap<V> {
    public V get(ItemLike var1);

    public void add(ItemLike var1, V var2);

    public void add(TagKey<Item> var1, V var2);

    public void remove(ItemLike var1);

    public void remove(TagKey<Item> var1);

    public void clear(ItemLike var1);

    public void clear(TagKey<Item> var1);
}

