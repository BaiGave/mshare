/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;
import net.fabricmc.fabric.impl.biome.WeightedPicker;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.jspecify.annotations.Nullable;

public final class TheEndBiomeData {
    public static final ThreadLocal<HolderGetter<Biome>> biomeRegistry = new ThreadLocal();
    public static final Set<ResourceKey<Biome>> ADDED_BIOMES = new HashSet<ResourceKey<Biome>>();
    private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BIOMES_MAP = new IdentityHashMap<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>>();
    private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_MIDLANDS_MAP = new IdentityHashMap<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>>();
    private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BARRENS_MAP = new IdentityHashMap<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>>();

    private TheEndBiomeData() {
    }

    public static void addEndBiomeReplacement(ResourceKey<Biome> replaced, ResourceKey<Biome> variant, double weight) {
        Preconditions.checkNotNull(replaced, "replaced entry is null");
        Preconditions.checkNotNull(variant, "variant entry is null");
        Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", (Object)weight);
        END_BIOMES_MAP.computeIfAbsent(replaced, key -> new WeightedPicker()).add(variant, weight);
        ADDED_BIOMES.add(variant);
    }

    public static void addEndMidlandsReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> midlands, double weight) {
        Preconditions.checkNotNull(highlands, "highlands entry is null");
        Preconditions.checkNotNull(midlands, "midlands entry is null");
        Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", (Object)weight);
        END_MIDLANDS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker()).add(midlands, weight);
        ADDED_BIOMES.add(midlands);
    }

    public static void addEndBarrensReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> barrens, double weight) {
        Preconditions.checkNotNull(highlands, "highlands entry is null");
        Preconditions.checkNotNull(barrens, "midlands entry is null");
        Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", (Object)weight);
        END_BARRENS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker()).add(barrens, weight);
        ADDED_BIOMES.add(barrens);
    }

    public static Overrides createOverrides(HolderGetter<Biome> biomes) {
        return new Overrides(biomes);
    }

    static {
        END_BIOMES_MAP.computeIfAbsent(Biomes.THE_END, key -> new WeightedPicker()).add(Biomes.THE_END, 1.0);
        END_BIOMES_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker()).add(Biomes.END_HIGHLANDS, 1.0);
        END_BIOMES_MAP.computeIfAbsent(Biomes.SMALL_END_ISLANDS, key -> new WeightedPicker()).add(Biomes.SMALL_END_ISLANDS, 1.0);
        END_MIDLANDS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker()).add(Biomes.END_MIDLANDS, 1.0);
        END_BARRENS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker()).add(Biomes.END_BARRENS, 1.0);
    }

    public static class Overrides {
        public final Set<Holder<Biome>> customBiomes;
        private final Holder<Biome> endMidlands;
        private final Holder<Biome> endBarrens;
        private final Holder<Biome> endHighlands;
        private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBiomesMap;
        private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endMidlandsMap;
        private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBarrensMap;
        private final Map<Climate.Sampler, ImprovedNoise> samplers = new WeakHashMap<Climate.Sampler, ImprovedNoise>();

        public Overrides(HolderGetter<Biome> biomeRegistry) {
            this.customBiomes = ADDED_BIOMES.stream().map(biomeRegistry::getOrThrow).collect(Collectors.toSet());
            this.endMidlands = biomeRegistry.getOrThrow(Biomes.END_MIDLANDS);
            this.endBarrens = biomeRegistry.getOrThrow(Biomes.END_BARRENS);
            this.endHighlands = biomeRegistry.getOrThrow(Biomes.END_HIGHLANDS);
            this.endBiomesMap = this.resolveOverrides(biomeRegistry, END_BIOMES_MAP, Biomes.THE_END);
            this.endMidlandsMap = this.resolveOverrides(biomeRegistry, END_MIDLANDS_MAP, Biomes.END_MIDLANDS);
            this.endBarrensMap = this.resolveOverrides(biomeRegistry, END_BARRENS_MAP, Biomes.END_BARRENS);
        }

        private @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> resolveOverrides(HolderGetter<Biome> biomeRegistry, Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> overrides, ResourceKey<Biome> vanillaKey) {
            Object2ObjectOpenCustomHashMap<Holder<Biome>, WeightedPicker<Holder<Biome>>> result = new Object2ObjectOpenCustomHashMap<Holder<Biome>, WeightedPicker<Holder<Biome>>>(overrides.size(), ResourceKeyHashStrategy.INSTANCE);
            for (Map.Entry<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> entry : overrides.entrySet()) {
                WeightedPicker<ResourceKey<Biome>> picker = entry.getValue();
                int count = picker.getEntryCount();
                if (count == 0 || count == 1 && entry.getKey() == vanillaKey) continue;
                result.put(biomeRegistry.getOrThrow(entry.getKey()), picker.map(biomeRegistry::getOrThrow));
            }
            return result.isEmpty() ? null : result;
        }

        public Holder<Biome> pick(int x, int y, int z, Climate.Sampler noise, Holder<Biome> vanillaBiome) {
            block5: {
                boolean isMidlands;
                block4: {
                    isMidlands = vanillaBiome.is(this.endMidlands::is);
                    if (isMidlands) break block4;
                    if (!vanillaBiome.is(this.endBarrens::is)) break block5;
                }
                Holder<Biome> highlandsReplacement = this.pick(this.endHighlands, this.endHighlands, this.endBiomesMap, x, z, noise);
                Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> map = isMidlands ? this.endMidlandsMap : this.endBarrensMap;
                return this.pick(highlandsReplacement, vanillaBiome, map, x, z, noise);
            }
            if (!END_BIOMES_MAP.containsKey(vanillaBiome.unwrapKey().orElseThrow())) {
                throw new IllegalStateException("Biome is not an End biome: " + String.valueOf(vanillaBiome));
            }
            return this.pick(vanillaBiome, vanillaBiome, this.endBiomesMap, x, z, noise);
        }

        private <T extends Holder<Biome>> T pick(T key, T defaultValue, Map<T, WeightedPicker<T>> pickers, int x, int z, Climate.Sampler noise) {
            WeightedPicker<T> picker;
            block6: {
                block5: {
                    if (pickers == null) {
                        return defaultValue;
                    }
                    picker = pickers.get(key);
                    if (picker == null) {
                        return defaultValue;
                    }
                    int count = picker.getEntryCount();
                    if (count == 0) break block5;
                    if (count != 1) break block6;
                    if (!key.is(this.endHighlands::is)) break block6;
                }
                return defaultValue;
            }
            return (T)((Holder)picker.pickFromNoise(((MultiNoiseSamplerHooks)((Object)noise)).fabric_getEndBiomesSampler(), (double)x / 64.0, 0.0, (double)z / 64.0));
        }
    }

    static enum ResourceKeyHashStrategy implements Hash.Strategy<Holder<?>>
    {
        INSTANCE;


        @Override
        public boolean equals(Holder<?> a, Holder<?> b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            if (a.kind() != b.kind()) {
                return false;
            }
            return a.unwrap().map(key -> b.unwrapKey().get() == key, b.value()::equals);
        }

        @Override
        public int hashCode(Holder<?> a) {
            if (a == null) {
                return 0;
            }
            return a.unwrap().map(System::identityHashCode, Object::hashCode);
        }
    }
}

