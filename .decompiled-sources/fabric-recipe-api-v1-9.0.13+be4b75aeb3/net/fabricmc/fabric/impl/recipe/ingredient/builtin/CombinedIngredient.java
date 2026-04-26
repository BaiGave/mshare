/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

abstract class CombinedIngredient
implements CustomIngredient {
    protected final List<Ingredient> ingredients;

    protected CombinedIngredient(List<Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("ALL or ANY ingredient must have at least one sub-ingredient");
        }
        this.ingredients = ingredients;
    }

    @Override
    public boolean requiresTesting() {
        for (Ingredient ingredient : this.ingredients) {
            if (!ingredient.requiresTesting()) continue;
            return true;
        }
        return false;
    }

    List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(this.ingredients.stream().map(Ingredient::display).toList());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CombinedIngredient)) {
            return false;
        }
        CombinedIngredient that = (CombinedIngredient)o;
        return this.ingredients.equals(that.ingredients);
    }

    public int hashCode() {
        return this.ingredients.hashCode();
    }

    static class Serializer<I extends CombinedIngredient>
    implements CustomIngredientSerializer<I> {
        private final Identifier identifier;
        private final MapCodec<I> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, I> streamCodec;

        Serializer(Identifier identifier, Function<List<Ingredient>, I> factory, MapCodec<I> codec) {
            this.identifier = identifier;
            this.codec = codec;
            this.streamCodec = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).map(factory, CombinedIngredient::getIngredients);
        }

        @Override
        public Identifier getIdentifier() {
            return this.identifier;
        }

        @Override
        public MapCodec<I> getCodec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, I> getStreamCodec() {
            return this.streamCodec;
        }
    }
}

