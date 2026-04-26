/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryResponse;
import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerboundCustomQueryAnswerPacket.class})
public class ServerboundCustomQueryAnswerPacketMixin {
    @Shadow
    @Final
    private static int MAX_PAYLOAD_SIZE;

    @Inject(method={"readPayload"}, at={@At(value="HEAD")}, cancellable=true)
    private static void readResponse(int queryId, FriendlyByteBuf buf, CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
        boolean hasPayload = buf.readBoolean();
        if (!hasPayload) {
            cir.setReturnValue(null);
            return;
        }
        cir.setReturnValue(new FriendlyByteBufLoginQueryResponse(PayloadHelper.read(buf, MAX_PAYLOAD_SIZE)));
    }
}

