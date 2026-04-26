/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class CustomDataIngredient
implements CustomIngredient {
    public static final CustomIngredientSerializer<CustomDataIngredient> SERIALIZER = new Serializer();
    private final Ingredient base;
    private final CompoundTag nbt;

    public CustomDataIngredient(Ingredient base, CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            throw new IllegalArgumentException("NBT cannot be null; use components ingredient for strict matching");
        }
        this.base = base;
        this.nbt = nbt;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!this.base.test(stack)) {
            return false;
        }
        CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);
        return nbt != null && nbt.matchedBy(this.nbt);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return this.base.items();
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(this.base.items().map(this::createEntryDisplay).toList());
    }

    private SlotDisplay createEntryDisplay(Holder<Item> holder) {
        DataComponentPatch data = DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(this.nbt)).build();
        return new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(holder, 1, data));
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private Ingredient getBase() {
        return this.base;
    }

    private CompoundTag getNbt() {
        return this.nbt;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        CustomDataIngredient that = (CustomDataIngredient)o;
        return this.base.equals(that.base) && this.nbt.equals(that.nbt);
    }

    public int hashCode() {
        return Objects.hash(this.base, this.nbt);
    }

    private static class Serializer
    implements CustomIngredientSerializer<CustomDataIngredient> {
        private static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "custom_data");
        private static final MapCodec<CustomDataIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.CODEC.fieldOf("base")).forGetter(CustomDataIngredient::getBase), ((MapCodec)TagParser.LENIENT_CODEC.fieldOf("nbt")).forGetter(CustomDataIngredient::getNbt)).apply((Applicative<CustomDataIngredient, ?>)instance, CustomDataIngredient::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CustomDataIngredient> PACKET_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, CustomDataIngredient::getBase, ByteBufCodecs.COMPOUND_TAG, CustomDataIngredient::getNbt, CustomDataIngredient::new);

        private Serializer() {
        }

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<CustomDataIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CustomDataIngredient> getStreamCodec() {
            return PACKET_CODEC;
        }
    }
}

