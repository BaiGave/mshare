/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class DifferenceIngredient
implements CustomIngredient {
    public static final CustomIngredientSerializer<DifferenceIngredient> SERIALIZER = new Serializer();
    private final Ingredient base;
    private final Ingredient subtracted;

    public DifferenceIngredient(Ingredient base, Ingredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.base.test(stack) && !this.subtracted.test(stack);
    }

    @Override
    public Stream<Holder<Item>> items() {
        List<Holder<Item>> subtractedMatchingItems = this.subtracted.items().toList();
        return this.base.items().filter(holder -> !subtractedMatchingItems.contains(holder));
    }

    @Override
    public boolean requiresTesting() {
        return this.base.requiresTesting() || this.subtracted.requiresTesting();
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private Ingredient getBase() {
        return this.base;
    }

    private Ingredient getSubtracted() {
        return this.subtracted;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DifferenceIngredient that = (DifferenceIngredient)o;
        return this.base.equals(that.base) && this.subtracted.equals(that.subtracted);
    }

    public int hashCode() {
        return Objects.hash(this.base, this.subtracted);
    }

    private static class Serializer
    implements CustomIngredientSerializer<DifferenceIngredient> {
        private static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "difference");
        private static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.CODEC.fieldOf("base")).forGetter(DifferenceIngredient::getBase), ((MapCodec)Ingredient.CODEC.fieldOf("subtracted")).forGetter(DifferenceIngredient::getSubtracted)).apply((Applicative<DifferenceIngredient, ?>)instance, DifferenceIngredient::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, DifferenceIngredient> PACKET_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::getBase, Ingredient.CONTENTS_STREAM_CODEC, DifferenceIngredient::getSubtracted, DifferenceIngredient::new);

        private Serializer() {
        }

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<DifferenceIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DifferenceIngredient> getStreamCodec() {
            return PACKET_CODEC;
        }
    }
}

