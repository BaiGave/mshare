/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.impl.tag.client.ClientTagsLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class ClientTagsImpl {
    private static final Map<TagKey<?>, ClientTagsLoader.LoadedTag> LOCAL_TAG_HIERARCHY = new ConcurrentHashMap();

    public static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> holder) {
        return ClientTagsImpl.isInWithLocalFallback(tagKey, holder, new HashSet<TagKey<T>>());
    }

    private static <T> boolean isInWithLocalFallback(TagKey<T> tagKey, Holder<T> holder, Set<TagKey<T>> checked) {
        if (checked.contains(tagKey)) {
            return false;
        }
        checked.add(tagKey);
        Optional<Registry<T>> maybeRegistry = ClientTagsImpl.getRegistry(tagKey);
        if (maybeRegistry.isPresent() && maybeRegistry.get().get(tagKey).isPresent()) {
            return holder.is(tagKey);
        }
        if (holder.unwrapKey().isEmpty()) {
            return false;
        }
        ClientTagsLoader.LoadedTag wt = ClientTagsImpl.getOrCreatePartiallySyncedTag(tagKey);
        if (wt.immediateChildIds().contains(holder.unwrapKey().get().identifier())) {
            return true;
        }
        for (TagKey<?> key : wt.immediateChildTags()) {
            if (ClientTagsImpl.isInWithLocalFallback(key, holder, checked)) {
                return true;
            }
            checked.add(key);
        }
        return false;
    }

    public static <T> Optional<? extends Registry<T>> getRegistry(TagKey<T> tagKey) {
        Optional<Registry<T>> maybeRegistry;
        Objects.requireNonNull(tagKey);
        if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null && Minecraft.getInstance().level.registryAccess() != null && (maybeRegistry = Minecraft.getInstance().level.registryAccess().lookup(tagKey.registry())).isPresent()) {
            return maybeRegistry;
        }
        return BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().identifier());
    }

    public static <T> Optional<Holder<T>> getHolder(TagKey<T> tagKey, T entry) {
        Optional<Registry<T>> maybeRegistry = ClientTagsImpl.getRegistry(tagKey);
        if (maybeRegistry.isEmpty() || !tagKey.isFor(maybeRegistry.get().key())) {
            return Optional.empty();
        }
        Registry<T> registry = maybeRegistry.get();
        Optional<ResourceKey<ResourceKey>> maybeKey = registry.getResourceKey(entry);
        return maybeKey.map(registry::getOrThrow);
    }

    public static ClientTagsLoader.LoadedTag getOrCreatePartiallySyncedTag(TagKey<?> tagKey) {
        ClientTagsLoader.LoadedTag loadedTag = LOCAL_TAG_HIERARCHY.get(tagKey);
        if (loadedTag == null) {
            loadedTag = ClientTagsLoader.loadTag(tagKey);
            LOCAL_TAG_HIERARCHY.put(tagKey, loadedTag);
        }
        return loadedTag;
    }
}

