/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagAliasGroup<T>(List<TagKey<T>> tags) {
    public static <T> Codec<TagAliasGroup<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
        return ((MapCodec)TagKey.codec(resourceKey).listOf().fieldOf("tags")).xmap(TagAliasGroup::new, TagAliasGroup::tags).codec();
    }
}

