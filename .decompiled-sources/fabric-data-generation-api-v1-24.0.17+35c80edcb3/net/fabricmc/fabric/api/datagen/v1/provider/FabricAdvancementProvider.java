/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

public abstract class FabricAdvancementProvider
implements DataProvider {
    protected final FabricPackOutput output;
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    protected FabricAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        this.output = output;
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registryLookup = registryLookup;
    }

    public abstract void generateAdvancement(HolderLookup.Provider var1, Consumer<AdvancementHolder> var2);

    protected Consumer<AdvancementHolder> withConditions(Consumer<AdvancementHolder> exporter, ResourceCondition ... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return advancement -> {
            FabricDataGenHelper.addConditions(advancement, conditions);
            exporter.accept((AdvancementHolder)advancement);
        };
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registryLookup.thenCompose(lookup -> {
            HashSet<Identifier> identifiers = Sets.newHashSet();
            HashSet<AdvancementHolder> advancements = Sets.newHashSet();
            this.generateAdvancement((HolderLookup.Provider)lookup, advancements::add);
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            ArrayList futures = new ArrayList();
            for (AdvancementHolder advancement : advancements) {
                if (!identifiers.add(advancement.id())) {
                    throw new IllegalStateException("Duplicate advancement " + String.valueOf(advancement.id()));
                }
                JsonObject advancementJson = Advancement.CODEC.encodeStart(ops, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
                FabricDataGenHelper.addConditions(advancementJson, FabricDataGenHelper.consumeConditions(advancement));
                futures.add(DataProvider.saveStable(output, advancementJson, this.getOutputPath(advancement)));
            }
            return CompletableFuture.allOf((CompletableFuture[])futures.toArray(CompletableFuture[]::new));
        });
    }

    private Path getOutputPath(AdvancementHolder advancement) {
        return this.pathProvider.json(advancement.id());
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}

