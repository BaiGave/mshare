/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe;

import java.util.Collection;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RecipeManager.class})
public abstract class RecipeManagerMixin
implements FabricRecipeManager {
    @Shadow
    private RecipeMap recipes;
    @Unique
    private SynchronizedRecipes synchronizedRecipes = SynchronizedRecipesImpl.EMPTY;

    @Inject(method={"apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"}, at={@At(value="HEAD")})
    private void updateSynchronizedRecipes(RecipeMap preparedRecipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        this.synchronizedRecipes = new SynchronizedRecipesImpl(preparedRecipes);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
        return this.recipes.byType(type);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level level) {
        return this.recipes.getRecipesFor(type, input, level);
    }

    @Override
    public SynchronizedRecipes getSynchronizedRecipes() {
        return this.synchronizedRecipes;
    }
}

