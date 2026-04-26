/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct FT_Size_RequestRec")
public class FT_Size_Request
extends Struct<FT_Size_Request>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int WIDTH;
    public static final int HEIGHT;
    public static final int HORIRESOLUTION;
    public static final int VERTRESOLUTION;

    protected FT_Size_Request(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_Size_Request create(long address, @Nullable ByteBuffer container) {
        return new FT_Size_Request(address, container);
    }

    public FT_Size_Request(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_Size_Request.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_Size_Request_Type")
    public int type() {
        return FT_Size_Request.ntype(this.address());
    }

    @NativeType(value="FT_Long")
    public long width() {
        return FT_Size_Request.nwidth(this.address());
    }

    @NativeType(value="FT_Long")
    public long height() {
        return FT_Size_Request.nheight(this.address());
    }

    @NativeType(value="FT_UInt")
    public int horiResolution() {
        return FT_Size_Request.nhoriResolution(this.address());
    }

    @NativeType(value="FT_UInt")
    public int vertResolution() {
        return FT_Size_Request.nvertResolution(this.address());
    }

    public FT_Size_Request type(@NativeType(value="FT_Size_Request_Type") int value) {
        FT_Size_Request.ntype(this.address(), value);
        return this;
    }

    public FT_Size_Request width(@NativeType(value="FT_Long") long value) {
        FT_Size_Request.nwidth(this.address(), value);
        return this;
    }

    public FT_Size_Request height(@NativeType(value="FT_Long") long value) {
        FT_Size_Request.nheight(this.address(), value);
        return this;
    }

    public FT_Size_Request horiResolution(@NativeType(value="FT_UInt") int value) {
        FT_Size_Request.nhoriResolution(this.address(), value);
        return this;
    }

    public FT_Size_Request vertResolution(@NativeType(value="FT_UInt") int value) {
        FT_Size_Request.nvertResolution(this.address(), value);
        return this;
    }

    public FT_Size_Request set(int type, long width, long height, int horiResolution, int vertResolution) {
        this.type(type);
        this.width(width);
        this.height(height);
        this.horiResolution(horiResolution);
        this.vertResolution(vertResolution);
        return this;
    }

    public FT_Size_Request set(FT_Size_Request src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static FT_Size_Request malloc() {
        return new FT_Size_Request(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static FT_Size_Request calloc() {
        return new FT_Size_Request(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static FT_Size_Request create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new FT_Size_Request(MemoryUtil.memAddress(container), container);
    }

    public static FT_Size_Request create(long address) {
        return new FT_Size_Request(address, null);
    }

    public static @Nullable FT_Size_Request createSafe(long address) {
        return address == 0L ? null : new FT_Size_Request(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(FT_Size_Request.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FT_Size_Request.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FT_Size_Request malloc(MemoryStack stack) {
        return new FT_Size_Request(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static FT_Size_Request calloc(MemoryStack stack) {
        return new FT_Size_Request(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ntype(long struct) {
        return MemoryUtil.memGetInt(struct + (long)TYPE);
    }

    public static long nwidth(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)WIDTH);
    }

    public static long nheight(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)HEIGHT);
    }

    public static int nhoriResolution(long struct) {
        return MemoryUtil.memGetInt(struct + (long)HORIRESOLUTION);
    }

    public static int nvertResolution(long struct) {
        return MemoryUtil.memGetInt(struct + (long)VERTRESOLUTION);
    }

    public static void ntype(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)TYPE, value);
    }

    public static void nwidth(long struct, long value) {
        MemoryUtil.memPutCLong(struct + (long)WIDTH, value);
    }

    public static void nheight(long struct, long value) {
        MemoryUtil.memPutCLong(struct + (long)HEIGHT, value);
    }

    public static void nhoriResolution(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)HORIRESOLUTION, value);
    }

    public static void nvertResolution(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)VERTRESOLUTION, value);
    }

    static {
        Struct.Layout layout = FT_Size_Request.__struct(FT_Size_Request.__member(4), FT_Size_Request.__member(CLONG_SIZE), FT_Size_Request.__member(CLONG_SIZE), FT_Size_Request.__member(4), FT_Size_Request.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        WIDTH = layout.offsetof(1);
        HEIGHT = layout.offsetof(2);
        HORIRESOLUTION = layout.offsetof(3);
        VERTRESOLUTION = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<FT_Size_Request, Buffer>
    implements NativeResource {
        private static final FT_Size_Request ELEMENT_FACTORY = FT_Size_Request.create(-1L);

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
        protected FT_Size_Request getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_Size_Request_Type")
        public int type() {
            return FT_Size_Request.ntype(this.address());
        }

        @NativeType(value="FT_Long")
        public long width() {
            return FT_Size_Request.nwidth(this.address());
        }

        @NativeType(value="FT_Long")
        public long height() {
            return FT_Size_Request.nheight(this.address());
        }

        @NativeType(value="FT_UInt")
        public int horiResolution() {
            return FT_Size_Request.nhoriResolution(this.address());
        }

        @NativeType(value="FT_UInt")
        public int vertResolution() {
            return FT_Size_Request.nvertResolution(this.address());
        }

        public Buffer type(@NativeType(value="FT_Size_Request_Type") int value) {
            FT_Size_Request.ntype(this.address(), value);
            return this;
        }

        public Buffer width(@NativeType(value="FT_Long") long value) {
            FT_Size_Request.nwidth(this.address(), value);
            return this;
        }

        public Buffer height(@NativeType(value="FT_Long") long value) {
            FT_Size_Request.nheight(this.address(), value);
            return this;
        }

        public Buffer horiResolution(@NativeType(value="FT_UInt") int value) {
            FT_Size_Request.nhoriResolution(this.address(), value);
            return this;
        }

        public Buffer vertResolution(@NativeType(value="FT_UInt") int value) {
            FT_Size_Request.nvertResolution(this.address(), value);
            return this;
        }
    }
}

