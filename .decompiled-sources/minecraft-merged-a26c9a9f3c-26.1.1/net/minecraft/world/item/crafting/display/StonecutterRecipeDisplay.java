/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting.display;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record StonecutterRecipeDisplay(SlotDisplay input, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<StonecutterRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)SlotDisplay.CODEC.fieldOf("input")).forGetter(StonecutterRecipeDisplay::input), ((MapCodec)SlotDisplay.CODEC.fieldOf("result")).forGetter(StonecutterRecipeDisplay::result), ((MapCodec)SlotDisplay.CODEC.fieldOf("crafting_station")).forGetter(StonecutterRecipeDisplay::craftingStation)).apply((Applicative<StonecutterRecipeDisplay, ?>)i, StonecutterRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StonecutterRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::input, SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::result, SlotDisplay.STREAM_CODEC, StonecutterRecipeDisplay::craftingStation, StonecutterRecipeDisplay::new);
    public static final RecipeDisplay.Type<StonecutterRecipeDisplay> TYPE = new RecipeDisplay.Type<StonecutterRecipeDisplay>(MAP_CODEC, STREAM_CODEC);

    public RecipeDisplay.Type<StonecutterRecipeDisplay> type() {
        return TYPE;
    }
}

