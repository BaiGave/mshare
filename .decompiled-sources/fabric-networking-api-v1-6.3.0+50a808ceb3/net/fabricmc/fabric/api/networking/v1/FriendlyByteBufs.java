/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public final class FriendlyByteBufs {
    private static final FriendlyByteBuf EMPTY_FRIENDLY_BYTE_BUF = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);

    public static FriendlyByteBuf empty() {
        return EMPTY_FRIENDLY_BYTE_BUF;
    }

    public static FriendlyByteBuf create() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    public static FriendlyByteBuf readBytes(ByteBuf buf, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.readBytes(length));
    }

    public static FriendlyByteBuf readSlice(ByteBuf buf, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.readSlice(length));
    }

    public static FriendlyByteBuf readRetainedSlice(ByteBuf buf, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.readRetainedSlice(length));
    }

    public static FriendlyByteBuf copy(ByteBuf buf) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.copy());
    }

    public static FriendlyByteBuf copy(ByteBuf buf, int index, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.copy(index, length));
    }

    public static FriendlyByteBuf slice(ByteBuf buf) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.slice());
    }

    public static FriendlyByteBuf retainedSlice(ByteBuf buf) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.retainedSlice());
    }

    public static FriendlyByteBuf slice(ByteBuf buf, int index, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.slice(index, length));
    }

    public static FriendlyByteBuf retainedSlice(ByteBuf buf, int index, int length) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.retainedSlice(index, length));
    }

    public static FriendlyByteBuf duplicate(ByteBuf buf) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.duplicate());
    }

    public static FriendlyByteBuf retainedDuplicate(ByteBuf buf) {
        Objects.requireNonNull(buf, "ByteBuf cannot be null");
        return new FriendlyByteBuf(buf.retainedDuplicate());
    }

    private FriendlyByteBufs() {
    }
}

