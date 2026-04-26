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

public class FT_Bitmap_Size
extends Struct<FT_Bitmap_Size> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int HEIGHT;
    public static final int WIDTH;
    public static final int SIZE;
    public static final int X_PPEM;
    public static final int Y_PPEM;

    protected FT_Bitmap_Size(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_Bitmap_Size create(long address, @Nullable ByteBuffer container) {
        return new FT_Bitmap_Size(address, container);
    }

    public FT_Bitmap_Size(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_Bitmap_Size.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_Short")
    public short height() {
        return FT_Bitmap_Size.nheight(this.address());
    }

    @NativeType(value="FT_Short")
    public short width() {
        return FT_Bitmap_Size.nwidth(this.address());
    }

    @NativeType(value="FT_Pos")
    public long size() {
        return FT_Bitmap_Size.nsize(this.address());
    }

    @NativeType(value="FT_Pos")
    public long x_ppem() {
        return FT_Bitmap_Size.nx_ppem(this.address());
    }

    @NativeType(value="FT_Pos")
    public long y_ppem() {
        return FT_Bitmap_Size.ny_ppem(this.address());
    }

    public static FT_Bitmap_Size create(long address) {
        return new FT_Bitmap_Size(address, null);
    }

    public static @Nullable FT_Bitmap_Size createSafe(long address) {
        return address == 0L ? null : new FT_Bitmap_Size(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static short nheight(long struct) {
        return MemoryUtil.memGetShort(struct + (long)HEIGHT);
    }

    public static short nwidth(long struct) {
        return MemoryUtil.memGetShort(struct + (long)WIDTH);
    }

    public static long nsize(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)SIZE);
    }

    public static long nx_ppem(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)X_PPEM);
    }

    public static long ny_ppem(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)Y_PPEM);
    }

    static {
        Struct.Layout layout = FT_Bitmap_Size.__struct(FT_Bitmap_Size.__member(2), FT_Bitmap_Size.__member(2), FT_Bitmap_Size.__member(CLONG_SIZE), FT_Bitmap_Size.__member(CLONG_SIZE), FT_Bitmap_Size.__member(CLONG_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        HEIGHT = layout.offsetof(0);
        WIDTH = layout.offsetof(1);
        SIZE = layout.offsetof(2);
        X_PPEM = layout.offsetof(3);
        Y_PPEM = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<FT_Bitmap_Size, Buffer> {
        private static final FT_Bitmap_Size ELEMENT_FACTORY = FT_Bitmap_Size.create(-1L);

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
        protected FT_Bitmap_Size getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_Short")
        public short height() {
            return FT_Bitmap_Size.nheight(this.address());
        }

        @NativeType(value="FT_Short")
        public short width() {
            return FT_Bitmap_Size.nwidth(this.address());
        }

        @NativeType(value="FT_Pos")
        public long size() {
            return FT_Bitmap_Size.nsize(this.address());
        }

        @NativeType(value="FT_Pos")
        public long x_ppem() {
            return FT_Bitmap_Size.nx_ppem(this.address());
        }

        @NativeType(value="FT_Pos")
        public long y_ppem() {
            return FT_Bitmap_Size.ny_ppem(this.address());
        }
    }
}

