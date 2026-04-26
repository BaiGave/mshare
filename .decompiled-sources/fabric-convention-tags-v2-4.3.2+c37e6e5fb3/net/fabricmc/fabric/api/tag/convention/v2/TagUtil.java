/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public final class TagUtil {
    public static final String C_TAG_NAMESPACE = "c";
    public static final String FABRIC_TAG_NAMESPACE = "fabric";

    private TagUtil() {
    }

    public static <T> boolean isIn(TagKey<T> tagKey, T entry) {
        return TagUtil.isIn(null, tagKey, entry);
    }

    public static <T> boolean isIn(@Nullable RegistryAccess registryAccess, TagKey<T> tagKey, T entry) {
        Registry<T> registry;
        Optional<ResourceKey<T>> maybeKey;
        Objects.requireNonNull(tagKey);
        Objects.requireNonNull(entry);
        Optional<Registry<Object>> maybeRegistry = registryAccess != null ? registryAccess.lookup(tagKey.registry()) : BuiltInRegistries.REGISTRY.getOptional(tagKey.registry().identifier());
        if (maybeRegistry.isPresent() && tagKey.isFor(maybeRegistry.get().key()) && (maybeKey = (registry = maybeRegistry.get()).getResourceKey(entry)).isPresent()) {
            return registry.getOrThrow(maybeKey.get()).is(tagKey);
        }
        return false;
    }
}

