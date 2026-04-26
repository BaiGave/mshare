/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;

public interface FabricTagAppender<E, T> {
    default public TagAppender<E, T> setReplace(boolean replace) {
        return (TagAppender)this;
    }

    default public TagAppender<E, T> forceAddTag(TagKey<T> tag) {
        return (TagAppender)this;
    }
}

