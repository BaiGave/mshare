/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryRequestPayload;
import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientboundCustomQueryPacket.class})
public class ClientboundCustomQueryPacketMixin {
    @Shadow
    @Final
    private static int MAX_PAYLOAD_SIZE;

    @Inject(method={"readPayload"}, at={@At(value="HEAD")}, cancellable=true)
    private static void readPayload(Identifier id, FriendlyByteBuf buf, CallbackInfoReturnable<CustomQueryPayload> cir) {
        cir.setReturnValue(new FriendlyByteBufLoginQueryRequestPayload(id, PayloadHelper.read(buf, MAX_PAYLOAD_SIZE)));
    }
}

