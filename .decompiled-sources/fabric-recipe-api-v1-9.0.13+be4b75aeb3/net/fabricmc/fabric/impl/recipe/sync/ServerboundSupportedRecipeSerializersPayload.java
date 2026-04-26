/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundSupportedRecipeSerializersPayload(Set<Identifier> synchronizedSerializers) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundSupportedRecipeSerializersPayload> CODEC = StreamCodec.composite(ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC), ServerboundSupportedRecipeSerializersPayload::synchronizedSerializers, ServerboundSupportedRecipeSerializersPayload::new);
    public static final CustomPacketPayload.Type<ServerboundSupportedRecipeSerializersPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("fabric", "recipe_sync/supported_serializers"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

