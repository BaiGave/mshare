/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.recipe.v1.ingredient;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface CustomIngredientSerializer<T extends CustomIngredient> {
    public static void register(CustomIngredientSerializer<?> serializer) {
        CustomIngredientImpl.registerSerializer(serializer);
    }

    public static @Nullable CustomIngredientSerializer<?> get(Identifier identifier) {
        return CustomIngredientImpl.getSerializer(identifier);
    }

    public Identifier getIdentifier();

    public MapCodec<T> getCodec();

    public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec();
}

