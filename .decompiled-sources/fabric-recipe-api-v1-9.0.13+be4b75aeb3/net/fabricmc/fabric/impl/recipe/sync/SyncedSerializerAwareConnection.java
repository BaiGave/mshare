/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import java.util.Set;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface SyncedSerializerAwareConnection {
    public void fabric_setSyncedRecipeSerializers(Set<RecipeSerializer<?>> var1);

    public Set<RecipeSerializer<?>> fabric_getSyncedRecipeSerializers();
}

