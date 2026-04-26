/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.payload;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class PayloadHelper {
    public static void write(FriendlyByteBuf byteBuf, FriendlyByteBuf data) {
        byteBuf.writeBytes(data.copy());
    }

    public static FriendlyByteBuf read(FriendlyByteBuf byteBuf, int maxSize) {
        PayloadHelper.assertSize(byteBuf, maxSize);
        FriendlyByteBuf newBuf = FriendlyByteBufs.create();
        newBuf.writeBytes(byteBuf.copy());
        byteBuf.skipBytes(byteBuf.readableBytes());
        return newBuf;
    }

    private static void assertSize(FriendlyByteBuf buf, int maxSize) {
        int size = buf.readableBytes();
        if (size < 0 || size > maxSize) {
            throw new IllegalArgumentException("Payload may not be larger than " + maxSize + " bytes");
        }
    }
}

