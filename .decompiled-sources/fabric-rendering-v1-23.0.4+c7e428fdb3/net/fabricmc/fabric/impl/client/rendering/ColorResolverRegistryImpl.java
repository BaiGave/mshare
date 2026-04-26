/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.UnmodifiableView;

public final class ColorResolverRegistryImpl {
    private static final Set<ColorResolver> ALL_RESOLVERS = new HashSet<ColorResolver>();
    private static final Set<ColorResolver> CUSTOM_RESOLVERS = new HashSet<ColorResolver>();
    private static final Set<ColorResolver> ALL_RESOLVERS_VIEW = Collections.unmodifiableSet(ALL_RESOLVERS);
    private static final Set<ColorResolver> CUSTOM_RESOLVERS_VIEW = Collections.unmodifiableSet(CUSTOM_RESOLVERS);

    private ColorResolverRegistryImpl() {
    }

    public static void register(ColorResolver resolver) {
        ALL_RESOLVERS.add(resolver);
        CUSTOM_RESOLVERS.add(resolver);
    }

    public static @UnmodifiableView Set<ColorResolver> getAllResolvers() {
        return ALL_RESOLVERS_VIEW;
    }

    public static @UnmodifiableView Set<ColorResolver> getCustomResolvers() {
        return CUSTOM_RESOLVERS_VIEW;
    }

    public static Reference2ReferenceMap<ColorResolver, BlockTintCache> createCustomCacheMap(Function<ColorResolver, BlockTintCache> cacheFactory) {
        Reference2ReferenceOpenHashMap<ColorResolver, BlockTintCache> map = new Reference2ReferenceOpenHashMap<ColorResolver, BlockTintCache>();
        for (ColorResolver resolver : CUSTOM_RESOLVERS) {
            map.put(resolver, cacheFactory.apply(resolver));
        }
        map.trim();
        return map;
    }

    static {
        ALL_RESOLVERS.add(BiomeColors.GRASS_COLOR_RESOLVER);
        ALL_RESOLVERS.add(BiomeColors.FOLIAGE_COLOR_RESOLVER);
        ALL_RESOLVERS.add(BiomeColors.WATER_COLOR_RESOLVER);
    }
}

