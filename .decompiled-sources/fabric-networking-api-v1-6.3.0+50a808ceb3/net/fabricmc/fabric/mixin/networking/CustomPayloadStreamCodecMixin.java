/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.networking.CustomPayloadTypeProvider;
import net.fabricmc.fabric.impl.networking.FabricCustomPayloadStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets={"net.minecraft.network.protocol.common.custom.CustomPacketPayload$1"})
public abstract class CustomPayloadStreamCodecMixin<B extends FriendlyByteBuf>
implements StreamCodec<B, CustomPacketPayload>,
FabricCustomPayloadStreamCodec<B> {
    @Unique
    private CustomPayloadTypeProvider<B> customPayloadTypeProvider;

    @Override
    public void fabric_setCustomPayloadTypeProvider(CustomPayloadTypeProvider<B> customPayloadTypeProvider) {
        if (this.customPayloadTypeProvider != null) {
            throw new IllegalStateException("Custom payload type provider is already set!");
        }
        this.customPayloadTypeProvider = customPayloadTypeProvider;
    }

    @WrapOperation(method={"writeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", "decode(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$1;findCodec(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/network/codec/StreamCodec;")})
    private StreamCodec<B, ? extends CustomPacketPayload> wrapGetCodec(@Coerce StreamCodec<B, CustomPacketPayload> instance, Identifier identifier, Operation<StreamCodec<B, CustomPacketPayload>> original, B buf) {
        CustomPacketPayload.TypeAndCodec<B, CustomPacketPayload> payloadType;
        if (this.customPayloadTypeProvider != null && (payloadType = this.customPayloadTypeProvider.get(buf, identifier)) != null) {
            return payloadType.codec();
        }
        return original.call(instance, identifier);
    }
}

