/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record HiddenByteBuf(ByteBuf contents) implements ReferenceCounted
{
    public HiddenByteBuf(ByteBuf contents) {
        this.contents = ByteBufUtil.ensureAccessible(contents);
    }

    public static Object pack(Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf)msg;
            return new HiddenByteBuf(buf);
        }
        return msg;
    }

    public static Object unpack(Object msg) {
        if (msg instanceof HiddenByteBuf) {
            HiddenByteBuf buf = (HiddenByteBuf)msg;
            return ByteBufUtil.ensureAccessible(buf.contents);
        }
        return msg;
    }

    @Override
    public int refCnt() {
        return this.contents.refCnt();
    }

    @Override
    public HiddenByteBuf retain() {
        this.contents.retain();
        return this;
    }

    @Override
    public HiddenByteBuf retain(int increment) {
        this.contents.retain(increment);
        return this;
    }

    @Override
    public HiddenByteBuf touch() {
        this.contents.touch();
        return this;
    }

    @Override
    public HiddenByteBuf touch(Object hint) {
        this.contents.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return this.contents.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.contents.release(decrement);
    }
}

