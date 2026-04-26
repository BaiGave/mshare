/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;
import net.minecraft.network.CipherBase;

public class CipherEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final CipherBase cipher;

    public CipherEncoder(Cipher cipher) {
        this.cipher = new CipherBase(cipher);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        this.cipher.encipher(msg, out);
    }
}

