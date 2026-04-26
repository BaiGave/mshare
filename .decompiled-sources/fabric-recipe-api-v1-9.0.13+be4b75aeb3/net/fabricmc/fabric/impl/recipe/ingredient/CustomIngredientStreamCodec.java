/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientSync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

public class CustomIngredientStreamCodec
implements StreamCodec<RegistryFriendlyByteBuf, Ingredient> {
    static final int PACKET_MARKER = -1;
    private final StreamCodec<RegistryFriendlyByteBuf, Ingredient> fallback;

    public CustomIngredientStreamCodec(StreamCodec<RegistryFriendlyByteBuf, Ingredient> fallback) {
        this.fallback = fallback;
    }

    @Override
    public Ingredient decode(RegistryFriendlyByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readVarInt() != -1) {
            buf.readerIndex(index);
            return (Ingredient)this.fallback.decode(buf);
        }
        Identifier type = buf.readIdentifier();
        CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Cannot deserialize custom ingredient of unknown type " + String.valueOf(type));
        }
        return ((CustomIngredient)serializer.getStreamCodec().decode(buf)).toVanilla();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, Ingredient value) {
        CustomIngredient customIngredient = value.getCustomIngredient();
        if (CustomIngredientStreamCodec.shouldEncodeFallback(customIngredient)) {
            this.fallback.encode(buf, value);
            return;
        }
        buf.writeVarInt(-1);
        buf.writeIdentifier(customIngredient.getSerializer().getIdentifier());
        StreamCodec<RegistryFriendlyByteBuf, ?> streamCodec = customIngredient.getSerializer().getStreamCodec();
        streamCodec.encode(buf, customIngredient);
    }

    static boolean shouldEncodeFallback(@Nullable CustomIngredient customIngredient) {
        if (customIngredient == null) {
            return true;
        }
        PacketContext context = PacketContext.get();
        if (context == null) {
            return false;
        }
        Set supportedIngredients = context.orElse(CustomIngredientSync.SUPPORTED_CUSTOM_INGREDIENTS, Set.of());
        return !supportedIngredients.contains(customIngredient.getSerializer().getIdentifier());
    }
}

