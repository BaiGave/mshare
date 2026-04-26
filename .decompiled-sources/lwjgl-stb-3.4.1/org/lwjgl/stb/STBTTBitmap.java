/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stbtt__bitmap")
public class STBTTBitmap
extends Struct<STBTTBitmap>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int W;
    public static final int H;
    public static final int STRIDE;
    public static final int PIXELS;

    protected STBTTBitmap(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected STBTTBitmap create(long address, @Nullable ByteBuffer container) {
        return new STBTTBitmap(address, container);
    }

    public STBTTBitmap(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBTTBitmap.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int w() {
        return STBTTBitmap.nw(this.address());
    }

    public int h() {
        return STBTTBitmap.nh(this.address());
    }

    public int stride() {
        return STBTTBitmap.nstride(this.address());
    }

    @NativeType(value="unsigned char *")
    public ByteBuffer pixels(int capacity) {
        return STBTTBitmap.npixels(this.address(), capacity);
    }

    public STBTTBitmap w(int value) {
        STBTTBitmap.nw(this.address(), value);
        return this;
    }

    public STBTTBitmap h(int value) {
        STBTTBitmap.nh(this.address(), value);
        return this;
    }

    public STBTTBitmap stride(int value) {
        STBTTBitmap.nstride(this.address(), value);
        return this;
    }

    public STBTTBitmap pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
        STBTTBitmap.npixels(this.address(), value);
        return this;
    }

    public STBTTBitmap set(int w, int h, int stride, ByteBuffer pixels) {
        this.w(w);
        this.h(h);
        this.stride(stride);
        this.pixels(pixels);
        return this;
    }

    public STBTTBitmap set(STBTTBitmap src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static STBTTBitmap malloc() {
        return new STBTTBitmap(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static STBTTBitmap calloc() {
        return new STBTTBitmap(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static STBTTBitmap create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new STBTTBitmap(MemoryUtil.memAddress(container), container);
    }

    public static STBTTBitmap create(long address) {
        return new STBTTBitmap(address, null);
    }

    public static @Nullable STBTTBitmap createSafe(long address) {
        return address == 0L ? null : new STBTTBitmap(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(STBTTBitmap.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBTTBitmap.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static STBTTBitmap malloc(MemoryStack stack) {
        return new STBTTBitmap(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static STBTTBitmap calloc(MemoryStack stack) {
        return new STBTTBitmap(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nw(long struct) {
        return MemoryUtil.memGetInt(struct + (long)W);
    }

    public static int nh(long struct) {
        return MemoryUtil.memGetInt(struct + (long)H);
    }

    public static int nstride(long struct) {
        return MemoryUtil.memGetInt(struct + (long)STRIDE);
    }

    public static ByteBuffer npixels(long struct, int capacity) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)PIXELS), capacity);
    }

    public static void nw(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)W, value);
    }

    public static void nh(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)H, value);
    }

    public static void nstride(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)STRIDE, value);
    }

    public static void npixels(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)PIXELS, MemoryUtil.memAddress(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)PIXELS));
    }

    static {
        Struct.Layout layout = STBTTBitmap.__struct(STBTTBitmap.__member(4), STBTTBitmap.__member(4), STBTTBitmap.__member(4), STBTTBitmap.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        W = layout.offsetof(0);
        H = layout.offsetof(1);
        STRIDE = layout.offsetof(2);
        PIXELS = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<STBTTBitmap, Buffer>
    implements NativeResource {
        private static final STBTTBitmap ELEMENT_FACTORY = STBTTBitmap.create(-1L);

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
        protected STBTTBitmap getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int w() {
            return STBTTBitmap.nw(this.address());
        }

        public int h() {
            return STBTTBitmap.nh(this.address());
        }

        public int stride() {
            return STBTTBitmap.nstride(this.address());
        }

        @NativeType(value="unsigned char *")
        public ByteBuffer pixels(int capacity) {
            return STBTTBitmap.npixels(this.address(), capacity);
        }

        public Buffer w(int value) {
            STBTTBitmap.nw(this.address(), value);
            return this;
        }

        public Buffer h(int value) {
            STBTTBitmap.nh(this.address(), value);
            return this;
        }

        public Buffer stride(int value) {
            STBTTBitmap.nstride(this.address(), value);
            return this;
        }

        public Buffer pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
            STBTTBitmap.npixels(this.address(), value);
            return this;
        }
    }
}

