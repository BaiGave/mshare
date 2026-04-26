/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.client.v1;

import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.impl.tag.client.ClientTagsImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class ClientTags {
    private ClientTags() {
    }

    public static Set<Identifier> getOrCreateLocalTag(TagKey<?> tagKey) {
        return ClientTagsImpl.getOrCreatePartiallySyncedTag(tagKey).completeIds();
    }

    public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, T entry) {
        Objects.requireNonNull(tagKey);
        Objects.requireNonNull(entry);
        return ClientTagsImpl.getHolder(tagKey, entry).map(re -> ClientTags.isInWithLocalFallback(tagKey, re)).orElse(false);
    }

    public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> holder) {
        Objects.requireNonNull(tagKey);
        Objects.requireNonNull(holder);
        return ClientTagsImpl.isInWithLocalFallback(tagKey, holder);
    }

    public static <T> boolean isInLocal(TagKey<T> tagKey, ResourceKey<T> resourceKey) {
        Objects.requireNonNull(tagKey);
        Objects.requireNonNull(resourceKey);
        if (tagKey.registry().identifier().equals(resourceKey.registry())) {
            Set<Identifier> ids = ClientTags.getOrCreateLocalTag(tagKey);
            return ids.contains(resourceKey.identifier());
        }
        return false;
    }
}

