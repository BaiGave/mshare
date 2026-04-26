/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricDataGenHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricDataGenHelper.class);
    public static final boolean ENABLED = System.getProperty("fabric-api.datagen") != null;
    private static final String OUTPUT_DIR = System.getProperty("fabric-api.datagen.output-dir");
    private static final boolean STRICT_VALIDATION = System.getProperty("fabric-api.datagen.strict-validation") != null;
    private static final @Nullable String MOD_ID_FILTER = System.getProperty("fabric-api.datagen.modid");
    private static final String ENTRYPOINT_KEY = "fabric-datagen";
    private static final Map<Object, ResourceCondition[]> CONDITIONS_MAP = new IdentityHashMap<Object, ResourceCondition[]>();

    private FabricDataGenHelper() {
    }

    public static void run() {
        try {
            FabricDataGenHelper.runInternal();
        }
        catch (Throwable t) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to run data generation", t);
            System.exit(-1);
        }
    }

    private static void runInternal() {
        Path outputDir = Paths.get(Objects.requireNonNull(OUTPUT_DIR, "No output dir provided with the 'fabric-api.datagen.output-dir' property"), new String[0]);
        List<EntrypointContainer<DataGeneratorEntrypoint>> dataGeneratorInitializers = FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, DataGeneratorEntrypoint.class);
        if (dataGeneratorInitializers.isEmpty()) {
            LOGGER.warn("No data generator entrypoints are defined. Implement {} and add your class to the '{}' entrypoint key in your fabric.mod.json.", (Object)DataGeneratorEntrypoint.class.getName(), (Object)ENTRYPOINT_KEY);
        }
        List<DataGeneratorEntrypoint> entrypoints = dataGeneratorInitializers.stream().map(EntrypointContainer::getEntrypoint).toList();
        CompletableFuture<HolderLookup.Provider> registriesFuture = CompletableFuture.supplyAsync(() -> FabricDataGenHelper.createHolderLookupProvider(entrypoints), Util.backgroundExecutor());
        Object2IntOpenHashMap jsonKeySortOrders = (Object2IntOpenHashMap)DataProvider.FIXED_ORDER_FIELDS;
        Object2IntOpenHashMap defaultJsonKeySortOrders = new Object2IntOpenHashMap(jsonKeySortOrders);
        for (EntrypointContainer<DataGeneratorEntrypoint> entrypointContainer : dataGeneratorInitializers) {
            String id = entrypointContainer.getProvider().getMetadata().getId();
            if (MOD_ID_FILTER != null && !id.equals(MOD_ID_FILTER)) continue;
            LOGGER.info("Running data generator for {}", (Object)id);
            try {
                DataGeneratorEntrypoint entrypoint = entrypointContainer.getEntrypoint();
                String effectiveModId = entrypoint.getEffectiveModId();
                ModContainer modContainer = entrypointContainer.getProvider();
                HashSet keys = new HashSet();
                entrypoint.addJsonKeySortOrders((key, value) -> {
                    Objects.requireNonNull(key, "Tried to register a priority for a null key");
                    jsonKeySortOrders.put(key, value);
                    keys.add(key);
                });
                if (effectiveModId != null) {
                    modContainer = FabricLoader.getInstance().getModContainer(effectiveModId).orElseThrow(() -> new RuntimeException("Failed to find effective mod container for mod id (%s)".formatted(effectiveModId)));
                }
                FabricDataGenerator dataGenerator = new FabricDataGenerator(outputDir, modContainer, STRICT_VALIDATION, registriesFuture);
                entrypoint.onInitializeDataGenerator(dataGenerator);
                dataGenerator.run();
                jsonKeySortOrders.keySet().removeAll(keys);
                jsonKeySortOrders.putAll(defaultJsonKeySortOrders);
            }
            catch (Throwable t) {
                throw new RuntimeException("Failed to run data generator from mod (%s)".formatted(id), t);
            }
        }
    }

    private static HolderLookup.Provider createHolderLookupProvider(List<DataGeneratorEntrypoint> dataGeneratorInitializers) {
        ArrayList<RegistrySetBuilder> builders = new ArrayList<RegistrySetBuilder>();
        builders.add(VanillaRegistries.BUILDER);
        for (DataGeneratorEntrypoint dataGeneratorEntrypoint : dataGeneratorInitializers) {
            RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
            dataGeneratorEntrypoint.buildRegistry(registrySetBuilder);
            builders.add(registrySetBuilder);
        }
        class BuilderData {
            final ResourceKey key;
            List<RegistrySetBuilder.RegistryBootstrap<?>> bootstrapFunctions;
            Lifecycle lifecycle;

            BuilderData(ResourceKey key) {
                this.key = key;
                this.bootstrapFunctions = new ArrayList();
                this.lifecycle = Lifecycle.stable();
            }

            void with(RegistrySetBuilder.RegistryStub<?> registryInfo) {
                this.bootstrapFunctions.add(registryInfo.bootstrap());
                this.lifecycle = registryInfo.lifecycle().add(this.lifecycle);
            }

            void apply(RegistrySetBuilder builder) {
                builder.add(this.key, this.lifecycle, this::bootstrap);
            }

            void bootstrap(BootstrapContext context) {
                for (RegistrySetBuilder.RegistryBootstrap<?> function : this.bootstrapFunctions) {
                    function.run(context);
                }
            }
        }
        HashMap<ResourceKey, BuilderData> builderDataMap = new HashMap<ResourceKey, BuilderData>();
        for (RegistryDataLoader.RegistryData<?> registryData : DynamicRegistries.getDynamicRegistries()) {
            builderDataMap.computeIfAbsent(registryData.key(), x$0 -> new BuilderData((ResourceKey)x$0));
        }
        for (RegistrySetBuilder registrySetBuilder : builders) {
            for (RegistrySetBuilder.RegistryStub<?> info : registrySetBuilder.entries) {
                builderDataMap.computeIfAbsent(info.key(), x$0 -> new BuilderData((ResourceKey)x$0)).with(info);
            }
        }
        RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
        for (BuilderData value : builderDataMap.values()) {
            value.apply(registrySetBuilder);
        }
        HolderLookup.Provider provider = registrySetBuilder.build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        VanillaRegistries.validateThatAllBiomeFeaturesHaveBiomeFilter(provider);
        return provider;
    }

    public static void addConditions(Object object, ResourceCondition[] conditions) {
        CONDITIONS_MAP.merge(object, conditions, ArrayUtils::addAll);
    }

    public static @Nullable ResourceCondition[] consumeConditions(Object object) {
        return CONDITIONS_MAP.remove(object);
    }

    public static void addConditions(JsonObject baseObject, ResourceCondition ... conditions) {
        if (baseObject.has("fabric:load_conditions")) {
            throw new IllegalArgumentException("Object already has a condition entry: " + String.valueOf(baseObject));
        }
        if (conditions == null || conditions.length == 0) {
            return;
        }
        baseObject.add("fabric:load_conditions", ResourceCondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, Arrays.asList(conditions)).getOrThrow());
    }

    public static void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

