/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class KeyTagProvider<T>
extends TagsProvider<T> {
    protected KeyTagProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, registryKey, lookupProvider);
    }

    protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> tag) {
        TagBuilder builder = this.getOrCreateRawBuilder(tag);
        return TagAppender.forBuilder(builder);
    }

    protected TagAppender<ResourceKey<T>, T> tag(TagKey<T> tag, boolean replace) {
        TagBuilder builder = this.getOrCreateRawBuilder(tag);
        builder.setReplace(replace);
        return TagAppender.forBuilder(builder);
    }
}

