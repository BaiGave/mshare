/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class CustomIngredientImpl
extends Ingredient {
    public static final String TYPE_KEY = "fabric:type";
    static final Map<Identifier, CustomIngredientSerializer<?>> REGISTERED_SERIALIZERS = new ConcurrentHashMap();
    public static final Codec<CustomIngredientSerializer<?>> CODEC = Identifier.CODEC.flatXmap(identifier -> Optional.ofNullable(REGISTERED_SERIALIZERS.get(identifier)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown custom ingredient serializer: " + String.valueOf(identifier))), serializer -> DataResult.success(serializer.getIdentifier()));
    private final CustomIngredient customIngredient;
    private @Nullable List<Holder<Item>> customMatchingItems;

    public static void registerSerializer(CustomIngredientSerializer<?> serializer) {
        Objects.requireNonNull(serializer.getIdentifier(), "CustomIngredientSerializer identifier may not be null.");
        if (REGISTERED_SERIALIZERS.putIfAbsent(serializer.getIdentifier(), serializer) != null) {
            throw new IllegalArgumentException("CustomIngredientSerializer with identifier " + String.valueOf(serializer.getIdentifier()) + " already registered.");
        }
    }

    public static @Nullable CustomIngredientSerializer<?> getSerializer(Identifier identifier) {
        Objects.requireNonNull(identifier, "Identifier may not be null.");
        return REGISTERED_SERIALIZERS.get(identifier);
    }

    public CustomIngredientImpl(CustomIngredient customIngredient) {
        super(HolderSet.direct(Items.STONE.builtInRegistryHolder()));
        this.customIngredient = customIngredient;
    }

    public List<Holder<Item>> getCustomMatchingItems() {
        if (this.customMatchingItems == null) {
            this.customMatchingItems = this.customIngredient.items().toList();
        }
        return this.customMatchingItems;
    }

    @Override
    public CustomIngredient getCustomIngredient() {
        return this.customIngredient;
    }

    @Override
    public boolean requiresTesting() {
        return this.customIngredient.requiresTesting();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return this.getCustomMatchingItems().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.getCustomMatchingItems().isEmpty();
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.customIngredient.test(stack);
    }

    @Override
    public boolean acceptsItem(Holder<Item> holder) {
        return this.getCustomMatchingItems().contains(holder);
    }

    @Override
    public SlotDisplay display() {
        return this.customIngredient.display();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomIngredientImpl)) {
            return false;
        }
        CustomIngredientImpl that = (CustomIngredientImpl)o;
        return this.customIngredient.equals(that.customIngredient);
    }

    public int hashCode() {
        return this.customIngredient.hashCode();
    }
}

