/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableSubProvider;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class SimpleFabricLootTableSubProvider
implements FabricLootTableSubProvider {
    protected final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;
    protected final ContextKeySet contextParamSet;

    public SimpleFabricLootTableSubProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture, ContextKeySet contextParamSet) {
        this.output = output;
        this.registryLookupFuture = registryLookupFuture;
        this.contextParamSet = contextParamSet;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return FabricLootTableProviderImpl.run(cache, this, this.contextParamSet, this.output, this.registryLookupFuture);
    }

    @Override
    public String getName() {
        return String.valueOf(Objects.requireNonNull((Identifier)LootContextParamSets.REGISTRY.inverse().get(this.contextParamSet), "Could not get id for loot context param set")) + " Loot Table";
    }
}

