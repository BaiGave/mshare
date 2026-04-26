/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.payload;

import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;

public record FriendlyByteBufLoginQueryResponse(FriendlyByteBuf data) implements CustomQueryAnswerPayload
{
    @Override
    public void write(FriendlyByteBuf buf) {
        PayloadHelper.write(buf, this.data());
    }
}

