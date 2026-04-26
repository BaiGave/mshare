/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.sync;

import java.util.Objects;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class RecipeSynchronization {
    public static final Identifier RECIPE_SYNC_EVENT_PHASE = RecipeSyncImpl.RECIPE_SYNC_EVENT_PHASE;

    private RecipeSynchronization() {
    }

    public static void synchronizeRecipeSerializer(RecipeSerializer<?> serializer) {
        Objects.requireNonNull(serializer, "serializer can't be null!");
        Objects.requireNonNull(serializer.streamCodec(), "StreamCodec can't be null!");
        RecipeSyncImpl.addSynchronizedSerializer(serializer);
    }
}

