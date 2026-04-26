/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T>
extends TagsProvider<T> {
    private final Function<T, ResourceKey<T>> keyExtractor;

    public IntrinsicHolderTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider, Function<T, ResourceKey<T>> keyExtractor) {
        super(output, registryKey, lookupProvider);
        this.keyExtractor = keyExtractor;
    }

    public IntrinsicHolderTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> parentProvider, Function<T, ResourceKey<T>> keyExtractor) {
        super(output, registryKey, lookupProvider, parentProvider);
        this.keyExtractor = keyExtractor;
    }

    protected TagAppender<T, T> tag(TagKey<T> tag) {
        TagBuilder builder = this.getOrCreateRawBuilder(tag);
        return TagAppender.forBuilder(builder).map(this.keyExtractor);
    }
}

