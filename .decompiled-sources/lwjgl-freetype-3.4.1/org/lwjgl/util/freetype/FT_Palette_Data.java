/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class FT_Palette_Data
extends Struct<FT_Palette_Data>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NUM_PALETTES;
    public static final int PALETTE_NAME_IDS;
    public static final int PALETTE_FLAGS;
    public static final int NUM_PALETTE_ENTRIES;
    public static final int PALETTE_ENTRY_NAME_IDS;

    protected FT_Palette_Data(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_Palette_Data create(long address, @Nullable ByteBuffer container) {
        return new FT_Palette_Data(address, container);
    }

    public FT_Palette_Data(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_Palette_Data.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_UShort")
    public short num_palettes() {
        return FT_Palette_Data.nnum_palettes(this.address());
    }

    @NativeType(value="FT_UShort const *")
    public @Nullable ShortBuffer palette_name_ids() {
        return FT_Palette_Data.npalette_name_ids(this.address());
    }

    @NativeType(value="FT_UShort const *")
    public @Nullable ShortBuffer palette_flags() {
        return FT_Palette_Data.npalette_flags(this.address());
    }

    @NativeType(value="FT_UShort")
    public short num_palette_entries() {
        return FT_Palette_Data.nnum_palette_entries(this.address());
    }

    @NativeType(value="FT_UShort const *")
    public @Nullable ShortBuffer palette_entry_name_ids() {
        return FT_Palette_Data.npalette_entry_name_ids(this.address());
    }

    public static FT_Palette_Data malloc() {
        return new FT_Palette_Data(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static FT_Palette_Data calloc() {
        return new FT_Palette_Data(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static FT_Palette_Data create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new FT_Palette_Data(MemoryUtil.memAddress(container), container);
    }

    public static FT_Palette_Data create(long address) {
        return new FT_Palette_Data(address, null);
    }

    public static @Nullable FT_Palette_Data createSafe(long address) {
        return address == 0L ? null : new FT_Palette_Data(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(FT_Palette_Data.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FT_Palette_Data.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FT_Palette_Data malloc(MemoryStack stack) {
        return new FT_Palette_Data(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static FT_Palette_Data calloc(MemoryStack stack) {
        return new FT_Palette_Data(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nnum_palettes(long struct) {
        return MemoryUtil.memGetShort(struct + (long)NUM_PALETTES);
    }

    public static @Nullable ShortBuffer npalette_name_ids(long struct) {
        return MemoryUtil.memShortBufferSafe(MemoryUtil.memGetAddress(struct + (long)PALETTE_NAME_IDS), Short.toUnsignedInt(FT_Palette_Data.nnum_palettes(struct)));
    }

    public static @Nullable ShortBuffer npalette_flags(long struct) {
        return MemoryUtil.memShortBufferSafe(MemoryUtil.memGetAddress(struct + (long)PALETTE_FLAGS), Short.toUnsignedInt(FT_Palette_Data.nnum_palettes(struct)));
    }

    public static short nnum_palette_entries(long struct) {
        return MemoryUtil.memGetShort(struct + (long)NUM_PALETTE_ENTRIES);
    }

    public static @Nullable ShortBuffer npalette_entry_name_ids(long struct) {
        return MemoryUtil.memShortBufferSafe(MemoryUtil.memGetAddress(struct + (long)PALETTE_ENTRY_NAME_IDS), Short.toUnsignedInt(FT_Palette_Data.nnum_palette_entries(struct)));
    }

    static {
        Struct.Layout layout = FT_Palette_Data.__struct(FT_Palette_Data.__member(2), FT_Palette_Data.__member(POINTER_SIZE), FT_Palette_Data.__member(POINTER_SIZE), FT_Palette_Data.__member(2), FT_Palette_Data.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NUM_PALETTES = layout.offsetof(0);
        PALETTE_NAME_IDS = layout.offsetof(1);
        PALETTE_FLAGS = layout.offsetof(2);
        NUM_PALETTE_ENTRIES = layout.offsetof(3);
        PALETTE_ENTRY_NAME_IDS = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<FT_Palette_Data, Buffer>
    implements NativeResource {
        private static final FT_Palette_Data ELEMENT_FACTORY = FT_Palette_Data.create(-1L);

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
        protected FT_Palette_Data getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_UShort")
        public short num_palettes() {
            return FT_Palette_Data.nnum_palettes(this.address());
        }

        @NativeType(value="FT_UShort const *")
        public @Nullable ShortBuffer palette_name_ids() {
            return FT_Palette_Data.npalette_name_ids(this.address());
        }

        @NativeType(value="FT_UShort const *")
        public @Nullable ShortBuffer palette_flags() {
            return FT_Palette_Data.npalette_flags(this.address());
        }

        @NativeType(value="FT_UShort")
        public short num_palette_entries() {
            return FT_Palette_Data.nnum_palette_entries(this.address());
        }

        @NativeType(value="FT_UShort const *")
        public @Nullable ShortBuffer palette_entry_name_ids() {
            return FT_Palette_Data.npalette_entry_name_ids(this.address());
        }
    }
}

