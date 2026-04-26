/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Block2ObjectMap<V> {
    public V get(Block var1);

    public void add(Block var1, V var2);

    public void add(TagKey<Block> var1, V var2);

    public void remove(Block var1);

    public void remove(TagKey<Block> var1);

    public void clear(Block var1);

    public void clear(TagKey<Block> var1);
}

