/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class FT_Span
extends Struct<FT_Span> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X;
    public static final int LEN;
    public static final int COVERAGE;

    protected FT_Span(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_Span create(long address, @Nullable ByteBuffer container) {
        return new FT_Span(address, container);
    }

    public FT_Span(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_Span.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public short x() {
        return FT_Span.nx(this.address());
    }

    @NativeType(value="unsigned short")
    public short len() {
        return FT_Span.nlen(this.address());
    }

    @NativeType(value="unsigned char")
    public byte coverage() {
        return FT_Span.ncoverage(this.address());
    }

    public static FT_Span create(long address) {
        return new FT_Span(address, null);
    }

    public static @Nullable FT_Span createSafe(long address) {
        return address == 0L ? null : new FT_Span(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static short nx(long struct) {
        return MemoryUtil.memGetShort(struct + (long)X);
    }

    public static short nlen(long struct) {
        return MemoryUtil.memGetShort(struct + (long)LEN);
    }

    public static byte ncoverage(long struct) {
        return MemoryUtil.memGetByte(struct + (long)COVERAGE);
    }

    static {
        Struct.Layout layout = FT_Span.__struct(FT_Span.__member(2), FT_Span.__member(2), FT_Span.__member(1));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        X = layout.offsetof(0);
        LEN = layout.offsetof(1);
        COVERAGE = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<FT_Span, Buffer> {
        private static final FT_Span ELEMENT_FACTORY = FT_Span.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected FT_Span getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public short x() {
            return FT_Span.nx(this.address());
        }

        @NativeType(value="unsigned short")
        public short len() {
            return FT_Span.nlen(this.address());
        }

        @NativeType(value="unsigned char")
        public byte coverage() {
            return FT_Span.ncoverage(this.address());
        }
    }
}

