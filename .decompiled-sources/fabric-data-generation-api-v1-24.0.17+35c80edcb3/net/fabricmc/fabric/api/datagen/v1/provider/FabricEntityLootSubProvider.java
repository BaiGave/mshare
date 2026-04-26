/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableSubProvider;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class FabricEntityLootSubProvider
extends EntityLootSubProvider
implements FabricLootTableSubProvider {
    private final FabricPackOutput output;
    private final Set<Identifier> excludedFromStrictValidation = new HashSet<Identifier>();
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    protected FabricEntityLootSubProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(FeatureFlags.REGISTRY.allFlags(), registriesFuture.join());
        this.output = output;
        this.registriesFuture = registriesFuture;
    }

    @Override
    public abstract void generate();

    public void excludeFromStrictValidation(EntityType<?> entityType) {
        this.excludedFromStrictValidation.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        this.generate();
        for (Map tables : this.map.values()) {
            for (Map.Entry entry : tables.entrySet()) {
                biConsumer.accept((ResourceKey)entry.getKey(), (LootTable.Builder)entry.getValue());
            }
        }
        if (this.output.isStrictValidationEnabled()) {
            HashSet missing = Sets.newHashSet();
            for (Identifier entityTypeId : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                if (!entityTypeId.getNamespace().equals(this.output.getModId())) continue;
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityTypeId);
                entityType.getDefaultLootTable().ifPresent(mainLootTableKey -> {
                    if (!mainLootTableKey.identifier().getNamespace().equals(this.output.getModId())) {
                        return;
                    }
                    Map tables = (Map)this.map.get(entityType);
                    if (tables == null || !tables.containsKey(mainLootTableKey)) {
                        missing.add(entityTypeId);
                    }
                });
            }
            missing.removeAll(this.excludedFromStrictValidation);
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return FabricLootTableProviderImpl.run(output, this, LootContextParamSets.ENTITY, this.output, this.registriesFuture);
    }

    @Override
    public String getName() {
        return "Entity Loot Tables";
    }
}

