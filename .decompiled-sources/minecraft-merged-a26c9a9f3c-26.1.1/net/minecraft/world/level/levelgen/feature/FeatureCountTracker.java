/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<ServerLevel, LevelData> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build(new CacheLoader<ServerLevel, LevelData>(){

        @Override
        public LevelData load(ServerLevel level) {
            return new LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap()), new MutableInt(0));
        }
    });

    public static void chunkDecorated(ServerLevel level) {
        try {
            data.get(level).chunksWithFeatures().increment();
        }
        catch (Exception e) {
            LOGGER.error("Failed to increment chunk count", e);
        }
    }

    public static void featurePlaced(ServerLevel level, ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
        try {
            data.get(level).featureData().computeInt(new FeatureData(feature, topFeature), (f, old) -> old == null ? 1 : old + 1);
        }
        catch (Exception e) {
            LOGGER.error("Failed to increment feature count", e);
        }
    }

    public static void clearCounts() {
        data.invalidateAll();
        LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        LOGGER.debug("Logging feature counts:");
        data.asMap().forEach((level, featureCounts) -> {
            String name = level.dimension().identifier().toString();
            boolean running = level.getServer().isRunning();
            HolderLookup.RegistryLookup featureRegistry = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
            String prefix = (running ? "running" : "dead") + " " + name;
            int chunks = featureCounts.chunksWithFeatures().intValue();
            LOGGER.debug("{} total_chunks: {}", (Object)prefix, (Object)chunks);
            featureCounts.featureData().forEach((arg_0, arg_1) -> FeatureCountTracker.lambda$logCounts$1(prefix, chunks, (Registry)featureRegistry, arg_0, arg_1));
        });
    }

    private static /* synthetic */ void lambda$logCounts$1(String prefix, int chunks, Registry featureRegistry, FeatureData data, int count) {
        Object[] objectArray = new Object[6];
        objectArray[0] = prefix;
        objectArray[1] = String.format(Locale.ROOT, "%10d", count);
        objectArray[2] = String.format(Locale.ROOT, "%10f", (double)count / (double)chunks);
        objectArray[3] = data.topFeature().flatMap(featureRegistry::getResourceKey).map(ResourceKey::identifier);
        objectArray[4] = data.feature().feature();
        objectArray[5] = data.feature();
        LOGGER.debug("{} {} {} {} {} {}", objectArray);
    }

    private record LevelData(Object2IntMap<FeatureData> featureData, MutableInt chunksWithFeatures) {
    }

    private record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
    }
}

