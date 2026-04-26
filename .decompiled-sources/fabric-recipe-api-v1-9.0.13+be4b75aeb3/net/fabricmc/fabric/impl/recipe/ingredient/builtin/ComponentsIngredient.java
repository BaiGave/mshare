/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.builtin;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class ComponentsIngredient
implements CustomIngredient {
    public static final CustomIngredientSerializer<ComponentsIngredient> SERIALIZER = new Serializer();
    private final Ingredient base;
    private final DataComponentPatch components;

    public ComponentsIngredient(Ingredient base, DataComponentPatch components) {
        if (components.isEmpty()) {
            throw new IllegalArgumentException("ComponentsIngredient must have at least one defined component");
        }
        this.base = base;
        this.components = components;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!this.base.test(stack)) {
            return false;
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : this.components.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            Optional<?> value = entry.getValue();
            if (value.isPresent()) {
                if (!stack.has(type)) {
                    return false;
                }
                if (Objects.equals(value.get(), stack.get(type))) continue;
                return false;
            }
            if (!stack.has(type)) continue;
            return false;
        }
        return true;
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
        return new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(holder, 1, this.components));
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

    private @Nullable DataComponentPatch getComponents() {
        return this.components;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ComponentsIngredient that = (ComponentsIngredient)o;
        return this.base.equals(that.base) && this.components.equals(that.components);
    }

    public int hashCode() {
        return Objects.hash(this.base, this.components);
    }

    private static class Serializer
    implements CustomIngredientSerializer<ComponentsIngredient> {
        private static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "components");
        private static final MapCodec<ComponentsIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Ingredient.CODEC.fieldOf("base")).forGetter(ComponentsIngredient::getBase), ((MapCodec)DataComponentPatch.CODEC.fieldOf("components")).forGetter(ComponentsIngredient::getComponents)).apply((Applicative<ComponentsIngredient, ?>)instance, ComponentsIngredient::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ComponentsIngredient> PACKET_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, ComponentsIngredient::getBase, DataComponentPatch.STREAM_CODEC, ComponentsIngredient::getComponents, ComponentsIngredient::new);

        private Serializer() {
        }

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<ComponentsIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ComponentsIngredient> getStreamCodec() {
            return PACKET_CODEC;
        }
    }
}

