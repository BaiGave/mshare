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

public record SmithingRecipeDisplay(SlotDisplay template, SlotDisplay base, SlotDisplay addition, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<SmithingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)SlotDisplay.CODEC.fieldOf("template")).forGetter(SmithingRecipeDisplay::template), ((MapCodec)SlotDisplay.CODEC.fieldOf("base")).forGetter(SmithingRecipeDisplay::base), ((MapCodec)SlotDisplay.CODEC.fieldOf("addition")).forGetter(SmithingRecipeDisplay::addition), ((MapCodec)SlotDisplay.CODEC.fieldOf("result")).forGetter(SmithingRecipeDisplay::result), ((MapCodec)SlotDisplay.CODEC.fieldOf("crafting_station")).forGetter(SmithingRecipeDisplay::craftingStation)).apply((Applicative<SmithingRecipeDisplay, ?>)i, SmithingRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmithingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, SmithingRecipeDisplay::template, SlotDisplay.STREAM_CODEC, SmithingRecipeDisplay::base, SlotDisplay.STREAM_CODEC, SmithingRecipeDisplay::addition, SlotDisplay.STREAM_CODEC, SmithingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, SmithingRecipeDisplay::craftingStation, SmithingRecipeDisplay::new);
    public static final RecipeDisplay.Type<SmithingRecipeDisplay> TYPE = new RecipeDisplay.Type<SmithingRecipeDisplay>(MAP_CODEC, STREAM_CODEC);

    public RecipeDisplay.Type<SmithingRecipeDisplay> type() {
        return TYPE;
    }
}

