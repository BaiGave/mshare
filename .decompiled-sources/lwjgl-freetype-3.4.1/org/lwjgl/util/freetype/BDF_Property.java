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

@NativeType(value="struct BDF_PropertyRec")
public class BDF_Property
extends Struct<BDF_Property>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int U;
    public static final int U_ATOM;
    public static final int U_INTEGER;
    public static final int U_CARDINAL;

    protected BDF_Property(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected BDF_Property create(long address, @Nullable ByteBuffer container) {
        return new BDF_Property(address, container);
    }

    public BDF_Property(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), BDF_Property.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="BDF_PropertyType")
    public int type() {
        return BDF_Property.ntype(this.address());
    }

    @NativeType(value="char const *")
    public @Nullable ByteBuffer u_atom() {
        return BDF_Property.nu_atom(this.address());
    }

    @NativeType(value="char const *")
    public @Nullable String u_atomString() {
        return BDF_Property.nu_atomString(this.address());
    }

    @NativeType(value="FT_Int32")
    public int u_integer() {
        return BDF_Property.nu_integer(this.address());
    }

    @NativeType(value="FT_UInt32")
    public int u_cardinal() {
        return BDF_Property.nu_cardinal(this.address());
    }

    public static BDF_Property malloc() {
        return new BDF_Property(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static BDF_Property calloc() {
        return new BDF_Property(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static BDF_Property create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new BDF_Property(MemoryUtil.memAddress(container), container);
    }

    public static BDF_Property create(long address) {
        return new BDF_Property(address, null);
    }

    public static @Nullable BDF_Property createSafe(long address) {
        return address == 0L ? null : new BDF_Property(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(BDF_Property.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = BDF_Property.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static BDF_Property malloc(MemoryStack stack) {
        return new BDF_Property(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static BDF_Property calloc(MemoryStack stack) {
        return new BDF_Property(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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

    public static @Nullable ByteBuffer nu_atom(long struct) {
        return MemoryUtil.memByteBufferNT1Safe(MemoryUtil.memGetAddress(struct + (long)U_ATOM));
    }

    public static @Nullable String nu_atomString(long struct) {
        return MemoryUtil.memASCIISafe(MemoryUtil.memGetAddress(struct + (long)U_ATOM));
    }

    public static int nu_integer(long struct) {
        return MemoryUtil.memGetInt(struct + (long)U_INTEGER);
    }

    public static int nu_cardinal(long struct) {
        return MemoryUtil.memGetInt(struct + (long)U_CARDINAL);
    }

    static {
        Struct.Layout layout = BDF_Property.__struct(BDF_Property.__member(4), BDF_Property.__union(BDF_Property.__member(POINTER_SIZE), BDF_Property.__member(4), BDF_Property.__member(4)));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        U = layout.offsetof(1);
        U_ATOM = layout.offsetof(2);
        U_INTEGER = layout.offsetof(3);
        U_CARDINAL = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<BDF_Property, Buffer>
    implements NativeResource {
        private static final BDF_Property ELEMENT_FACTORY = BDF_Property.create(-1L);

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
        protected BDF_Property getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="BDF_PropertyType")
        public int type() {
            return BDF_Property.ntype(this.address());
        }

        @NativeType(value="char const *")
        public @Nullable ByteBuffer u_atom() {
            return BDF_Property.nu_atom(this.address());
        }

        @NativeType(value="char const *")
        public @Nullable String u_atomString() {
            return BDF_Property.nu_atomString(this.address());
        }

        @NativeType(value="FT_Int32")
        public int u_integer() {
            return BDF_Property.nu_integer(this.address());
        }

        @NativeType(value="FT_UInt32")
        public int u_cardinal() {
            return BDF_Property.nu_cardinal(this.address());
        }
    }
}

