/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableSubProvider;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class FabricBlockLootSubProvider
extends BlockLootSubProvider
implements FabricLootTableSubProvider {
    private final FabricPackOutput output;
    private final Set<Identifier> excludedFromStrictValidation = new HashSet<Identifier>();
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    protected FabricBlockLootSubProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registriesFuture.join());
        this.output = packOutput;
        this.registriesFuture = registriesFuture;
    }

    @Override
    public abstract void generate();

    public void excludeFromStrictValidation(Block block) {
        this.excludedFromStrictValidation.add(BuiltInRegistries.BLOCK.getKey(block));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        this.generate();
        for (Map.Entry entry : this.map.entrySet()) {
            ResourceKey resourceKey = (ResourceKey)entry.getKey();
            biConsumer.accept(resourceKey, (LootTable.Builder)entry.getValue());
        }
        if (this.output.isStrictValidationEnabled()) {
            HashSet<Identifier> missing = Sets.newHashSet();
            for (Identifier blockId : BuiltInRegistries.BLOCK.keySet()) {
                Optional<ResourceKey<LootTable>> blockLootTableId;
                if (!blockId.getNamespace().equals(this.output.getModId()) || !(blockLootTableId = BuiltInRegistries.BLOCK.getValue(blockId).getLootTable()).isPresent() || !blockLootTableId.get().identifier().getNamespace().equals(this.output.getModId()) || this.map.containsKey(blockLootTableId.get())) continue;
                missing.add(blockId);
            }
            missing.removeAll(this.excludedFromStrictValidation);
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing loot table(s) for %s".formatted(missing));
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return FabricLootTableProviderImpl.run(output, this, LootContextParamSets.BLOCK, this.output, this.registriesFuture);
    }

    @Override
    public String getName() {
        return "Block Loot Tables";
    }
}

