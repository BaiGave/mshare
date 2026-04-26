/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.Nullable;

public interface SyncedSerializerAwarePreparedRecipe {
    public @Nullable List<RecipeHolder<?>> fabric_getRecipesBySyncedSerializer(RecipeSerializer<?> var1);
}

