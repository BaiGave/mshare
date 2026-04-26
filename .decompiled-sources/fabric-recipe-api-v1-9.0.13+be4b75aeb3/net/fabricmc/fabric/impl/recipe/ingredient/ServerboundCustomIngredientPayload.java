/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundCustomIngredientPayload(int protocolVersion, Set<Identifier> registeredSerializers) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundCustomIngredientPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundCustomIngredientPayload::protocolVersion, ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC), ServerboundCustomIngredientPayload::registeredSerializers, ServerboundCustomIngredientPayload::new);
    public static final CustomPacketPayload.Type<ServerboundCustomIngredientPayload> TYPE = new CustomPacketPayload.Type(CustomIngredientSync.PACKET_ID);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

