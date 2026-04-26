/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stbtt_bakedchar")
public class STBTTBakedChar
extends Struct<STBTTBakedChar>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X0;
    public static final int Y0;
    public static final int X1;
    public static final int Y1;
    public static final int XOFF;
    public static final int YOFF;
    public static final int XADVANCE;

    protected STBTTBakedChar(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected STBTTBakedChar create(long address, @Nullable ByteBuffer container) {
        return new STBTTBakedChar(address, container);
    }

    public STBTTBakedChar(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBTTBakedChar.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="unsigned short")
    public short x0() {
        return STBTTBakedChar.nx0(this.address());
    }

    @NativeType(value="unsigned short")
    public short y0() {
        return STBTTBakedChar.ny0(this.address());
    }

    @NativeType(value="unsigned short")
    public short x1() {
        return STBTTBakedChar.nx1(this.address());
    }

    @NativeType(value="unsigned short")
    public short y1() {
        return STBTTBakedChar.ny1(this.address());
    }

    public float xoff() {
        return STBTTBakedChar.nxoff(this.address());
    }

    public float yoff() {
        return STBTTBakedChar.nyoff(this.address());
    }

    public float xadvance() {
        return STBTTBakedChar.nxadvance(this.address());
    }

    public static STBTTBakedChar malloc() {
        return new STBTTBakedChar(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static STBTTBakedChar calloc() {
        return new STBTTBakedChar(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static STBTTBakedChar create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new STBTTBakedChar(MemoryUtil.memAddress(container), container);
    }

    public static STBTTBakedChar create(long address) {
        return new STBTTBakedChar(address, null);
    }

    public static @Nullable STBTTBakedChar createSafe(long address) {
        return address == 0L ? null : new STBTTBakedChar(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(STBTTBakedChar.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBTTBakedChar.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static STBTTBakedChar malloc(MemoryStack stack) {
        return new STBTTBakedChar(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static STBTTBakedChar calloc(MemoryStack stack) {
        return new STBTTBakedChar(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nx0(long struct) {
        return MemoryUtil.memGetShort(struct + (long)X0);
    }

    public static short ny0(long struct) {
        return MemoryUtil.memGetShort(struct + (long)Y0);
    }

    public static short nx1(long struct) {
        return MemoryUtil.memGetShort(struct + (long)X1);
    }

    public static short ny1(long struct) {
        return MemoryUtil.memGetShort(struct + (long)Y1);
    }

    public static float nxoff(long struct) {
        return MemoryUtil.memGetFloat(struct + (long)XOFF);
    }

    public static float nyoff(long struct) {
        return MemoryUtil.memGetFloat(struct + (long)YOFF);
    }

    public static float nxadvance(long struct) {
        return MemoryUtil.memGetFloat(struct + (long)XADVANCE);
    }

    static {
        Struct.Layout layout = STBTTBakedChar.__struct(STBTTBakedChar.__member(2), STBTTBakedChar.__member(2), STBTTBakedChar.__member(2), STBTTBakedChar.__member(2), STBTTBakedChar.__member(4), STBTTBakedChar.__member(4), STBTTBakedChar.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        X0 = layout.offsetof(0);
        Y0 = layout.offsetof(1);
        X1 = layout.offsetof(2);
        Y1 = layout.offsetof(3);
        XOFF = layout.offsetof(4);
        YOFF = layout.offsetof(5);
        XADVANCE = layout.offsetof(6);
    }

    public static class Buffer
    extends StructBuffer<STBTTBakedChar, Buffer>
    implements NativeResource {
        private static final STBTTBakedChar ELEMENT_FACTORY = STBTTBakedChar.create(-1L);

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
        protected STBTTBakedChar getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="unsigned short")
        public short x0() {
            return STBTTBakedChar.nx0(this.address());
        }

        @NativeType(value="unsigned short")
        public short y0() {
            return STBTTBakedChar.ny0(this.address());
        }

        @NativeType(value="unsigned short")
        public short x1() {
            return STBTTBakedChar.nx1(this.address());
        }

        @NativeType(value="unsigned short")
        public short y1() {
            return STBTTBakedChar.ny1(this.address());
        }

        public float xoff() {
            return STBTTBakedChar.nxoff(this.address());
        }

        public float yoff() {
            return STBTTBakedChar.nyoff(this.address());
        }

        public float xadvance() {
            return STBTTBakedChar.nxadvance(this.address());
        }
    }
}

