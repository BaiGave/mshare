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

public class FT_SfntName
extends Struct<FT_SfntName>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int PLATFORM_ID;
    public static final int ENCODING_ID;
    public static final int LANGUAGE_ID;
    public static final int NAME_ID;
    public static final int STRING;
    public static final int STRING_LEN;

    protected FT_SfntName(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_SfntName create(long address, @Nullable ByteBuffer container) {
        return new FT_SfntName(address, container);
    }

    public FT_SfntName(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_SfntName.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_UShort")
    public short platform_id() {
        return FT_SfntName.nplatform_id(this.address());
    }

    @NativeType(value="FT_UShort")
    public short encoding_id() {
        return FT_SfntName.nencoding_id(this.address());
    }

    @NativeType(value="FT_UShort")
    public short language_id() {
        return FT_SfntName.nlanguage_id(this.address());
    }

    @NativeType(value="FT_UShort")
    public short name_id() {
        return FT_SfntName.nname_id(this.address());
    }

    @NativeType(value="FT_Byte *")
    public ByteBuffer string() {
        return FT_SfntName.nstring(this.address());
    }

    @NativeType(value="FT_UInt")
    public int string_len() {
        return FT_SfntName.nstring_len(this.address());
    }

    public static FT_SfntName malloc() {
        return new FT_SfntName(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static FT_SfntName calloc() {
        return new FT_SfntName(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static FT_SfntName create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new FT_SfntName(MemoryUtil.memAddress(container), container);
    }

    public static FT_SfntName create(long address) {
        return new FT_SfntName(address, null);
    }

    public static @Nullable FT_SfntName createSafe(long address) {
        return address == 0L ? null : new FT_SfntName(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(FT_SfntName.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FT_SfntName.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FT_SfntName malloc(MemoryStack stack) {
        return new FT_SfntName(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static FT_SfntName calloc(MemoryStack stack) {
        return new FT_SfntName(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nplatform_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)PLATFORM_ID);
    }

    public static short nencoding_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)ENCODING_ID);
    }

    public static short nlanguage_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)LANGUAGE_ID);
    }

    public static short nname_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)NAME_ID);
    }

    public static ByteBuffer nstring(long struct) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)STRING), FT_SfntName.nstring_len(struct));
    }

    public static int nstring_len(long struct) {
        return MemoryUtil.memGetInt(struct + (long)STRING_LEN);
    }

    static {
        Struct.Layout layout = FT_SfntName.__struct(FT_SfntName.__member(2), FT_SfntName.__member(2), FT_SfntName.__member(2), FT_SfntName.__member(2), FT_SfntName.__member(POINTER_SIZE), FT_SfntName.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        PLATFORM_ID = layout.offsetof(0);
        ENCODING_ID = layout.offsetof(1);
        LANGUAGE_ID = layout.offsetof(2);
        NAME_ID = layout.offsetof(3);
        STRING = layout.offsetof(4);
        STRING_LEN = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<FT_SfntName, Buffer>
    implements NativeResource {
        private static final FT_SfntName ELEMENT_FACTORY = FT_SfntName.create(-1L);

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
        protected FT_SfntName getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_UShort")
        public short platform_id() {
            return FT_SfntName.nplatform_id(this.address());
        }

        @NativeType(value="FT_UShort")
        public short encoding_id() {
            return FT_SfntName.nencoding_id(this.address());
        }

        @NativeType(value="FT_UShort")
        public short language_id() {
            return FT_SfntName.nlanguage_id(this.address());
        }

        @NativeType(value="FT_UShort")
        public short name_id() {
            return FT_SfntName.nname_id(this.address());
        }

        @NativeType(value="FT_Byte *")
        public ByteBuffer string() {
            return FT_SfntName.nstring(this.address());
        }

        @NativeType(value="FT_UInt")
        public int string_len() {
            return FT_SfntName.nstring_len(this.address());
        }
    }
}

