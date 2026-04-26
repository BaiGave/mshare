/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.CLongBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class TT_Header
extends Struct<TT_Header> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TABLE_VERSION;
    public static final int FONT_REVISION;
    public static final int CHECKSUM_ADJUST;
    public static final int MAGIC_NUMBER;
    public static final int FLAGS;
    public static final int UNITS_PER_EM;
    public static final int CREATED;
    public static final int MODIFIED;
    public static final int XMIN;
    public static final int YMIN;
    public static final int XMAX;
    public static final int YMAX;
    public static final int MAC_STYLE;
    public static final int LOWEST_REC_PPEM;
    public static final int FONT_DIRECTION;
    public static final int INDEX_TO_LOC_FORMAT;
    public static final int GLYPH_DATA_FORMAT;

    protected TT_Header(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected TT_Header create(long address, @Nullable ByteBuffer container) {
        return new TT_Header(address, container);
    }

    public TT_Header(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), TT_Header.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_Fixed")
    public long Table_Version() {
        return TT_Header.nTable_Version(this.address());
    }

    @NativeType(value="FT_Fixed")
    public long Font_Revision() {
        return TT_Header.nFont_Revision(this.address());
    }

    @NativeType(value="FT_Long")
    public long CheckSum_Adjust() {
        return TT_Header.nCheckSum_Adjust(this.address());
    }

    @NativeType(value="FT_Long")
    public long Magic_Number() {
        return TT_Header.nMagic_Number(this.address());
    }

    @NativeType(value="FT_UShort")
    public short Flags() {
        return TT_Header.nFlags(this.address());
    }

    @NativeType(value="FT_UShort")
    public short Units_Per_EM() {
        return TT_Header.nUnits_Per_EM(this.address());
    }

    @NativeType(value="FT_ULong[2]")
    public CLongBuffer Created() {
        return TT_Header.nCreated(this.address());
    }

    @NativeType(value="FT_ULong")
    public long Created(int index) {
        return TT_Header.nCreated(this.address(), index);
    }

    @NativeType(value="FT_ULong[2]")
    public CLongBuffer Modified() {
        return TT_Header.nModified(this.address());
    }

    @NativeType(value="FT_ULong")
    public long Modified(int index) {
        return TT_Header.nModified(this.address(), index);
    }

    @NativeType(value="FT_Short")
    public short xMin() {
        return TT_Header.nxMin(this.address());
    }

    @NativeType(value="FT_Short")
    public short yMin() {
        return TT_Header.nyMin(this.address());
    }

    @NativeType(value="FT_Short")
    public short xMax() {
        return TT_Header.nxMax(this.address());
    }

    @NativeType(value="FT_Short")
    public short yMax() {
        return TT_Header.nyMax(this.address());
    }

    @NativeType(value="FT_UShort")
    public short Mac_Style() {
        return TT_Header.nMac_Style(this.address());
    }

    @NativeType(value="FT_UShort")
    public short Lowest_Rec_PPEM() {
        return TT_Header.nLowest_Rec_PPEM(this.address());
    }

    @NativeType(value="FT_Short")
    public short Font_Direction() {
        return TT_Header.nFont_Direction(this.address());
    }

    @NativeType(value="FT_Short")
    public short Index_To_Loc_Format() {
        return TT_Header.nIndex_To_Loc_Format(this.address());
    }

    @NativeType(value="FT_Short")
    public short Glyph_Data_Format() {
        return TT_Header.nGlyph_Data_Format(this.address());
    }

    public static TT_Header create(long address) {
        return new TT_Header(address, null);
    }

    public static @Nullable TT_Header createSafe(long address) {
        return address == 0L ? null : new TT_Header(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static long nTable_Version(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)TABLE_VERSION);
    }

    public static long nFont_Revision(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)FONT_REVISION);
    }

    public static long nCheckSum_Adjust(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)CHECKSUM_ADJUST);
    }

    public static long nMagic_Number(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)MAGIC_NUMBER);
    }

    public static short nFlags(long struct) {
        return MemoryUtil.memGetShort(struct + (long)FLAGS);
    }

    public static short nUnits_Per_EM(long struct) {
        return MemoryUtil.memGetShort(struct + (long)UNITS_PER_EM);
    }

    public static CLongBuffer nCreated(long struct) {
        return MemoryUtil.memCLongBuffer(struct + (long)CREATED, 2);
    }

    public static long nCreated(long struct, int index) {
        return MemoryUtil.memGetCLong(struct + (long)CREATED + Checks.check(index, 2) * (long)CLONG_SIZE);
    }

    public static CLongBuffer nModified(long struct) {
        return MemoryUtil.memCLongBuffer(struct + (long)MODIFIED, 2);
    }

    public static long nModified(long struct, int index) {
        return MemoryUtil.memGetCLong(struct + (long)MODIFIED + Checks.check(index, 2) * (long)CLONG_SIZE);
    }

    public static short nxMin(long struct) {
        return MemoryUtil.memGetShort(struct + (long)XMIN);
    }

    public static short nyMin(long struct) {
        return MemoryUtil.memGetShort(struct + (long)YMIN);
    }

    public static short nxMax(long struct) {
        return MemoryUtil.memGetShort(struct + (long)XMAX);
    }

    public static short nyMax(long struct) {
        return MemoryUtil.memGetShort(struct + (long)YMAX);
    }

    public static short nMac_Style(long struct) {
        return MemoryUtil.memGetShort(struct + (long)MAC_STYLE);
    }

    public static short nLowest_Rec_PPEM(long struct) {
        return MemoryUtil.memGetShort(struct + (long)LOWEST_REC_PPEM);
    }

    public static short nFont_Direction(long struct) {
        return MemoryUtil.memGetShort(struct + (long)FONT_DIRECTION);
    }

    public static short nIndex_To_Loc_Format(long struct) {
        return MemoryUtil.memGetShort(struct + (long)INDEX_TO_LOC_FORMAT);
    }

    public static short nGlyph_Data_Format(long struct) {
        return MemoryUtil.memGetShort(struct + (long)GLYPH_DATA_FORMAT);
    }

    static {
        Struct.Layout layout = TT_Header.__struct(TT_Header.__member(CLONG_SIZE), TT_Header.__member(CLONG_SIZE), TT_Header.__member(CLONG_SIZE), TT_Header.__member(CLONG_SIZE), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__array(CLONG_SIZE, 2), TT_Header.__array(CLONG_SIZE, 2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2), TT_Header.__member(2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TABLE_VERSION = layout.offsetof(0);
        FONT_REVISION = layout.offsetof(1);
        CHECKSUM_ADJUST = layout.offsetof(2);
        MAGIC_NUMBER = layout.offsetof(3);
        FLAGS = layout.offsetof(4);
        UNITS_PER_EM = layout.offsetof(5);
        CREATED = layout.offsetof(6);
        MODIFIED = layout.offsetof(7);
        XMIN = layout.offsetof(8);
        YMIN = layout.offsetof(9);
        XMAX = layout.offsetof(10);
        YMAX = layout.offsetof(11);
        MAC_STYLE = layout.offsetof(12);
        LOWEST_REC_PPEM = layout.offsetof(13);
        FONT_DIRECTION = layout.offsetof(14);
        INDEX_TO_LOC_FORMAT = layout.offsetof(15);
        GLYPH_DATA_FORMAT = layout.offsetof(16);
    }

    public static class Buffer
    extends StructBuffer<TT_Header, Buffer> {
        private static final TT_Header ELEMENT_FACTORY = TT_Header.create(-1L);

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
        protected TT_Header getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_Fixed")
        public long Table_Version() {
            return TT_Header.nTable_Version(this.address());
        }

        @NativeType(value="FT_Fixed")
        public long Font_Revision() {
            return TT_Header.nFont_Revision(this.address());
        }

        @NativeType(value="FT_Long")
        public long CheckSum_Adjust() {
            return TT_Header.nCheckSum_Adjust(this.address());
        }

        @NativeType(value="FT_Long")
        public long Magic_Number() {
            return TT_Header.nMagic_Number(this.address());
        }

        @NativeType(value="FT_UShort")
        public short Flags() {
            return TT_Header.nFlags(this.address());
        }

        @NativeType(value="FT_UShort")
        public short Units_Per_EM() {
            return TT_Header.nUnits_Per_EM(this.address());
        }

        @NativeType(value="FT_ULong[2]")
        public CLongBuffer Created() {
            return TT_Header.nCreated(this.address());
        }

        @NativeType(value="FT_ULong")
        public long Created(int index) {
            return TT_Header.nCreated(this.address(), index);
        }

        @NativeType(value="FT_ULong[2]")
        public CLongBuffer Modified() {
            return TT_Header.nModified(this.address());
        }

        @NativeType(value="FT_ULong")
        public long Modified(int index) {
            return TT_Header.nModified(this.address(), index);
        }

        @NativeType(value="FT_Short")
        public short xMin() {
            return TT_Header.nxMin(this.address());
        }

        @NativeType(value="FT_Short")
        public short yMin() {
            return TT_Header.nyMin(this.address());
        }

        @NativeType(value="FT_Short")
        public short xMax() {
            return TT_Header.nxMax(this.address());
        }

        @NativeType(value="FT_Short")
        public short yMax() {
            return TT_Header.nyMax(this.address());
        }

        @NativeType(value="FT_UShort")
        public short Mac_Style() {
            return TT_Header.nMac_Style(this.address());
        }

        @NativeType(value="FT_UShort")
        public short Lowest_Rec_PPEM() {
            return TT_Header.nLowest_Rec_PPEM(this.address());
        }

        @NativeType(value="FT_Short")
        public short Font_Direction() {
            return TT_Header.nFont_Direction(this.address());
        }

        @NativeType(value="FT_Short")
        public short Index_To_Loc_Format() {
            return TT_Header.nIndex_To_Loc_Format(this.address());
        }

        @NativeType(value="FT_Short")
        public short Glyph_Data_Format() {
            return TT_Header.nGlyph_Data_Format(this.address());
        }
    }
}

