/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public abstract class FabricRecipeProvider
extends RecipeProvider.Runner {
    protected final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public FabricRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
        this.output = output;
        this.registriesFuture = registriesFuture;
    }

    @Override
    protected abstract RecipeProvider createRecipeProvider(HolderLookup.Provider var1, RecipeOutput var2);

    protected RecipeOutput withConditions(final RecipeOutput output, final ResourceCondition ... conditions) {
        Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");
        return new RecipeOutput(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                FabricDataGenHelper.addConditions(recipe, conditions);
                output.accept(key, recipe, advancementHolder);
            }

            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void includeRootAdvancement() {
            }

            @Override
            public Identifier getRecipeIdentifier(Identifier recipeId) {
                return output.getRecipeIdentifier(recipeId);
            }
        };
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registriesFuture.thenCompose(registries -> {
            final HashSet generatedRecipes = Sets.newHashSet();
            ArrayList list = new ArrayList();
            RecipeProvider recipeProvider = this.createRecipeProvider((HolderLookup.Provider)registries, new RecipeOutput(){
                final /* synthetic */ HolderLookup.Provider val$registries;
                final /* synthetic */ List val$list;
                final /* synthetic */ CachedOutput val$output;
                final /* synthetic */ FabricRecipeProvider this$0;
                {
                    this.val$registries = provider;
                    this.val$list = list;
                    this.val$output = cachedOutput;
                    FabricRecipeProvider fabricRecipeProvider = this$0;
                    Objects.requireNonNull(fabricRecipeProvider);
                    this.this$0 = fabricRecipeProvider;
                }

                @Override
                public void accept(ResourceKey<Recipe<?>> recipeKey, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
                    Identifier identifier = recipeKey.identifier();
                    if (!generatedRecipes.add(identifier)) {
                        throw new IllegalStateException("Duplicate recipe " + String.valueOf(identifier));
                    }
                    RegistryOps<JsonElement> registryOps = this.val$registries.createSerializationContext(JsonOps.INSTANCE);
                    JsonObject recipeJson = Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow(IllegalStateException::new).getAsJsonObject();
                    ResourceCondition[] conditions = FabricDataGenHelper.consumeConditions(recipe);
                    FabricDataGenHelper.addConditions(recipeJson, conditions);
                    PackOutput.PathProvider recipesPathResolver = this.this$0.output.createRegistryElementsPathProvider(Registries.RECIPE);
                    PackOutput.PathProvider advancementsPathResolver = this.this$0.output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
                    this.val$list.add(DataProvider.saveStable(this.val$output, recipeJson, recipesPathResolver.json(identifier)));
                    if (advancement != null) {
                        JsonObject advancementJson = Advancement.CODEC.encodeStart(registryOps, advancement.value()).getOrThrow(IllegalStateException::new).getAsJsonObject();
                        FabricDataGenHelper.addConditions(advancementJson, conditions);
                        this.val$list.add(DataProvider.saveStable(this.val$output, advancementJson, advancementsPathResolver.json(advancement.id())));
                    }
                }

                @Override
                public Advancement.Builder advancement() {
                    return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                }

                @Override
                public void includeRootAdvancement() {
                }

                @Override
                public Identifier getRecipeIdentifier(Identifier recipeId) {
                    return this.this$0.getRecipeIdentifier(recipeId);
                }
            });
            recipeProvider.buildRecipes();
            return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
        });
    }

    protected Identifier getRecipeIdentifier(Identifier identifier) {
        return Identifier.fromNamespaceAndPath(this.output.getModId(), identifier.getPath());
    }
}

