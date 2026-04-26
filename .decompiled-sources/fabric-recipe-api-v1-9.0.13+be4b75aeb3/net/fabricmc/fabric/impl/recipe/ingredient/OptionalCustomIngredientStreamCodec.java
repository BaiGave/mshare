/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Optional;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientStreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;

public class OptionalCustomIngredientStreamCodec
implements StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> {
    private final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> fallback;

    public OptionalCustomIngredientStreamCodec(StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> fallback) {
        this.fallback = fallback;
    }

    @Override
    public Optional<Ingredient> decode(RegistryFriendlyByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readVarInt() != -1) {
            buf.readerIndex(index);
            return (Optional)this.fallback.decode(buf);
        }
        Identifier type = buf.readIdentifier();
        CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + String.valueOf(type));
        }
        return Optional.of(((CustomIngredient)serializer.getStreamCodec().decode(buf)).toVanilla());
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, Optional<Ingredient> value) {
        if (value.isEmpty()) {
            this.fallback.encode(buf, value);
            return;
        }
        CustomIngredient customIngredient = value.get().getCustomIngredient();
        if (CustomIngredientStreamCodec.shouldEncodeFallback(customIngredient)) {
            this.fallback.encode(buf, value);
            return;
        }
        buf.writeVarInt(-1);
        buf.writeIdentifier(customIngredient.getSerializer().getIdentifier());
        StreamCodec<RegistryFriendlyByteBuf, ?> streamCodec = customIngredient.getSerializer().getStreamCodec();
        streamCodec.encode(buf, customIngredient);
    }
}

