/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen.loot;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableSubProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;

public final class FabricLootTableProviderImpl {
    public static CompletableFuture<?> run(CachedOutput cache, FabricLootTableSubProvider provider, ContextKeySet contextParamSet, FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        HashMap builders = Maps.newHashMap();
        HashMap conditionMap = new HashMap();
        return registryLookupFuture.thenCompose(lookup -> {
            provider.generate((resourceKey, builder) -> {
                ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(builder);
                conditionMap.put(resourceKey.identifier(), conditions);
                if (builders.put(resourceKey.identifier(), builder.setParamSet(contextParamSet).build()) != null) {
                    throw new IllegalStateException("Duplicate loot table " + String.valueOf(resourceKey.identifier()));
                }
            });
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            ArrayList futures = new ArrayList();
            for (Map.Entry entry : builders.entrySet()) {
                JsonObject tableJson = (JsonObject)LootTable.DIRECT_CODEC.encodeStart(ops, (LootTable)entry.getValue()).getOrThrow(IllegalStateException::new);
                FabricDataGenHelper.addConditions(tableJson, (ResourceCondition[])conditionMap.remove(entry.getKey()));
                futures.add(DataProvider.saveStable(cache, tableJson, FabricLootTableProviderImpl.getOutputPath(packOutput, (Identifier)entry.getKey())));
            }
            return CompletableFuture.allOf((CompletableFuture[])futures.toArray(CompletableFuture[]::new));
        });
    }

    private static Path getOutputPath(FabricPackOutput packOutput, Identifier lootTableId) {
        return packOutput.createRegistryElementsPathProvider(Registries.LOOT_TABLE).json(lootTableId);
    }

    private FabricLootTableProviderImpl() {
    }
}

