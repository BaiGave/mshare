/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.payload;

import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.Identifier;

public record FriendlyByteBufLoginQueryRequestPayload(Identifier id, FriendlyByteBuf data) implements CustomQueryPayload
{
    @Override
    public void write(FriendlyByteBuf buf) {
        PayloadHelper.write(buf, this.data());
    }
}

