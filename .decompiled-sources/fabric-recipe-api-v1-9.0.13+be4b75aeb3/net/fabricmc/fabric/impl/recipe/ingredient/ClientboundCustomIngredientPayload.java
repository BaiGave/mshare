/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundCustomIngredientPayload(int protocolVersion) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomIngredientPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundCustomIngredientPayload::protocolVersion, ClientboundCustomIngredientPayload::new);
    public static final CustomPacketPayload.Type<ClientboundCustomIngredientPayload> TYPE = new CustomPacketPayload.Type(CustomIngredientSync.PACKET_ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

