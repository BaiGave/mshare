/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting.display;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ShapedCraftingRecipeDisplay(int width, int height, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<ShapedCraftingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.INT.fieldOf("width")).forGetter(ShapedCraftingRecipeDisplay::width), ((MapCodec)Codec.INT.fieldOf("height")).forGetter(ShapedCraftingRecipeDisplay::height), ((MapCodec)SlotDisplay.CODEC.listOf().fieldOf("ingredients")).forGetter(ShapedCraftingRecipeDisplay::ingredients), ((MapCodec)SlotDisplay.CODEC.fieldOf("result")).forGetter(ShapedCraftingRecipeDisplay::result), ((MapCodec)SlotDisplay.CODEC.fieldOf("crafting_station")).forGetter(ShapedCraftingRecipeDisplay::craftingStation)).apply((Applicative<ShapedCraftingRecipeDisplay, ?>)i, ShapedCraftingRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedCraftingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ShapedCraftingRecipeDisplay::width, ByteBufCodecs.VAR_INT, ShapedCraftingRecipeDisplay::height, SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ShapedCraftingRecipeDisplay::ingredients, SlotDisplay.STREAM_CODEC, ShapedCraftingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, ShapedCraftingRecipeDisplay::craftingStation, ShapedCraftingRecipeDisplay::new);
    public static final RecipeDisplay.Type<ShapedCraftingRecipeDisplay> TYPE = new RecipeDisplay.Type<ShapedCraftingRecipeDisplay>(MAP_CODEC, STREAM_CODEC);

    public ShapedCraftingRecipeDisplay {
        if (ingredients.size() != width * height) {
            throw new IllegalArgumentException("Invalid shaped recipe display contents");
        }
    }

    public RecipeDisplay.Type<ShapedCraftingRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.ingredients.stream().allMatch(e -> e.isEnabled(enabledFeatures)) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}

