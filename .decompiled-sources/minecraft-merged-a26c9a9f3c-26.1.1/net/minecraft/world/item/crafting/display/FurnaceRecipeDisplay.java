/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting.display;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record FurnaceRecipeDisplay(SlotDisplay ingredient, SlotDisplay fuel, SlotDisplay result, SlotDisplay craftingStation, int duration, float experience) implements RecipeDisplay
{
    public static final MapCodec<FurnaceRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)SlotDisplay.CODEC.fieldOf("ingredient")).forGetter(FurnaceRecipeDisplay::ingredient), ((MapCodec)SlotDisplay.CODEC.fieldOf("fuel")).forGetter(FurnaceRecipeDisplay::fuel), ((MapCodec)SlotDisplay.CODEC.fieldOf("result")).forGetter(FurnaceRecipeDisplay::result), ((MapCodec)SlotDisplay.CODEC.fieldOf("crafting_station")).forGetter(FurnaceRecipeDisplay::craftingStation), ((MapCodec)Codec.INT.fieldOf("duration")).forGetter(FurnaceRecipeDisplay::duration), ((MapCodec)Codec.FLOAT.fieldOf("experience")).forGetter(FurnaceRecipeDisplay::experience)).apply((Applicative<FurnaceRecipeDisplay, ?>)i, FurnaceRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FurnaceRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, FurnaceRecipeDisplay::ingredient, SlotDisplay.STREAM_CODEC, FurnaceRecipeDisplay::fuel, SlotDisplay.STREAM_CODEC, FurnaceRecipeDisplay::result, SlotDisplay.STREAM_CODEC, FurnaceRecipeDisplay::craftingStation, ByteBufCodecs.VAR_INT, FurnaceRecipeDisplay::duration, ByteBufCodecs.FLOAT, FurnaceRecipeDisplay::experience, FurnaceRecipeDisplay::new);
    public static final RecipeDisplay.Type<FurnaceRecipeDisplay> TYPE = new RecipeDisplay.Type<FurnaceRecipeDisplay>(MAP_CODEC, STREAM_CODEC);

    public RecipeDisplay.Type<FurnaceRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.ingredient.isEnabled(enabledFeatures) && this.fuel().isEnabled(enabledFeatures) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}

