/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting.display;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ShapelessCraftingRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<ShapelessCraftingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)SlotDisplay.CODEC.listOf().fieldOf("ingredients")).forGetter(ShapelessCraftingRecipeDisplay::ingredients), ((MapCodec)SlotDisplay.CODEC.fieldOf("result")).forGetter(ShapelessCraftingRecipeDisplay::result), ((MapCodec)SlotDisplay.CODEC.fieldOf("crafting_station")).forGetter(ShapelessCraftingRecipeDisplay::craftingStation)).apply((Applicative<ShapelessCraftingRecipeDisplay, ?>)i, ShapelessCraftingRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessCraftingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ShapelessCraftingRecipeDisplay::ingredients, SlotDisplay.STREAM_CODEC, ShapelessCraftingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, ShapelessCraftingRecipeDisplay::craftingStation, ShapelessCraftingRecipeDisplay::new);
    public static final RecipeDisplay.Type<ShapelessCraftingRecipeDisplay> TYPE = new RecipeDisplay.Type<ShapelessCraftingRecipeDisplay>(MAP_CODEC, STREAM_CODEC);

    public RecipeDisplay.Type<ShapelessCraftingRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.ingredients.stream().allMatch(e -> e.isEnabled(enabledFeatures)) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}

