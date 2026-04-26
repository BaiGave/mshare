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
import org.lwjgl.util.freetype.FT_Glyph;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Size_Metrics;
import org.lwjgl.util.freetype.FT_Vector;

@NativeType(value="struct FT_SvgGlyphRec")
public class FT_SvgGlyph
extends Struct<FT_SvgGlyph> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int ROOT;
    public static final int SVG_DOCUMENT;
    public static final int SVG_DOCUMENT_LENGTH;
    public static final int GLYPH_INDEX;
    public static final int METRICS;
    public static final int UNITS_PER_EM;
    public static final int START_GLYPH_ID;
    public static final int END_GLYPH_ID;
    public static final int TRANSFORM;
    public static final int DELTA;

    protected FT_SvgGlyph(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_SvgGlyph create(long address, @Nullable ByteBuffer container) {
        return new FT_SvgGlyph(address, container);
    }

    public FT_SvgGlyph(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_SvgGlyph.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_GlyphRec")
    public FT_Glyph root() {
        return FT_SvgGlyph.nroot(this.address());
    }

    @NativeType(value="FT_Byte *")
    public ByteBuffer svg_document() {
        return FT_SvgGlyph.nsvg_document(this.address());
    }

    @NativeType(value="FT_ULong")
    public long svg_document_length() {
        return FT_SvgGlyph.nsvg_document_length(this.address());
    }

    @NativeType(value="FT_UInt")
    public int glyph_index() {
        return FT_SvgGlyph.nglyph_index(this.address());
    }

    public FT_Size_Metrics metrics() {
        return FT_SvgGlyph.nmetrics(this.address());
    }

    @NativeType(value="FT_UShort")
    public short units_per_EM() {
        return FT_SvgGlyph.nunits_per_EM(this.address());
    }

    @NativeType(value="FT_UShort")
    public short start_glyph_id() {
        return FT_SvgGlyph.nstart_glyph_id(this.address());
    }

    @NativeType(value="FT_UShort")
    public short end_glyph_id() {
        return FT_SvgGlyph.nend_glyph_id(this.address());
    }

    public FT_Matrix transform() {
        return FT_SvgGlyph.ntransform(this.address());
    }

    public FT_Vector delta() {
        return FT_SvgGlyph.ndelta(this.address());
    }

    public static FT_SvgGlyph create(long address) {
        return new FT_SvgGlyph(address, null);
    }

    public static @Nullable FT_SvgGlyph createSafe(long address) {
        return address == 0L ? null : new FT_SvgGlyph(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FT_Glyph nroot(long struct) {
        return FT_Glyph.create(struct + (long)ROOT);
    }

    public static ByteBuffer nsvg_document(long struct) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)SVG_DOCUMENT), (int)FT_SvgGlyph.nsvg_document_length(struct));
    }

    public static long nsvg_document_length(long struct) {
        return MemoryUtil.memGetCLong(struct + (long)SVG_DOCUMENT_LENGTH);
    }

    public static int nglyph_index(long struct) {
        return MemoryUtil.memGetInt(struct + (long)GLYPH_INDEX);
    }

    public static FT_Size_Metrics nmetrics(long struct) {
        return FT_Size_Metrics.create(struct + (long)METRICS);
    }

    public static short nunits_per_EM(long struct) {
        return MemoryUtil.memGetShort(struct + (long)UNITS_PER_EM);
    }

    public static short nstart_glyph_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)START_GLYPH_ID);
    }

    public static short nend_glyph_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)END_GLYPH_ID);
    }

    public static FT_Matrix ntransform(long struct) {
        return FT_Matrix.create(struct + (long)TRANSFORM);
    }

    public static FT_Vector ndelta(long struct) {
        return FT_Vector.create(struct + (long)DELTA);
    }

    static {
        Struct.Layout layout = FT_SvgGlyph.__struct(FT_SvgGlyph.__member(FT_Glyph.SIZEOF, FT_Glyph.ALIGNOF), FT_SvgGlyph.__member(POINTER_SIZE), FT_SvgGlyph.__member(CLONG_SIZE), FT_SvgGlyph.__member(4), FT_SvgGlyph.__member(FT_Size_Metrics.SIZEOF, FT_Size_Metrics.ALIGNOF), FT_SvgGlyph.__member(2), FT_SvgGlyph.__member(2), FT_SvgGlyph.__member(2), FT_SvgGlyph.__member(FT_Matrix.SIZEOF, FT_Matrix.ALIGNOF), FT_SvgGlyph.__member(FT_Vector.SIZEOF, FT_Vector.ALIGNOF));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        ROOT = layout.offsetof(0);
        SVG_DOCUMENT = layout.offsetof(1);
        SVG_DOCUMENT_LENGTH = layout.offsetof(2);
        GLYPH_INDEX = layout.offsetof(3);
        METRICS = layout.offsetof(4);
        UNITS_PER_EM = layout.offsetof(5);
        START_GLYPH_ID = layout.offsetof(6);
        END_GLYPH_ID = layout.offsetof(7);
        TRANSFORM = layout.offsetof(8);
        DELTA = layout.offsetof(9);
    }

    public static class Buffer
    extends StructBuffer<FT_SvgGlyph, Buffer> {
        private static final FT_SvgGlyph ELEMENT_FACTORY = FT_SvgGlyph.create(-1L);

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
        protected FT_SvgGlyph getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_GlyphRec")
        public FT_Glyph root() {
            return FT_SvgGlyph.nroot(this.address());
        }

        @NativeType(value="FT_Byte *")
        public ByteBuffer svg_document() {
            return FT_SvgGlyph.nsvg_document(this.address());
        }

        @NativeType(value="FT_ULong")
        public long svg_document_length() {
            return FT_SvgGlyph.nsvg_document_length(this.address());
        }

        @NativeType(value="FT_UInt")
        public int glyph_index() {
            return FT_SvgGlyph.nglyph_index(this.address());
        }

        public FT_Size_Metrics metrics() {
            return FT_SvgGlyph.nmetrics(this.address());
        }

        @NativeType(value="FT_UShort")
        public short units_per_EM() {
            return FT_SvgGlyph.nunits_per_EM(this.address());
        }

        @NativeType(value="FT_UShort")
        public short start_glyph_id() {
            return FT_SvgGlyph.nstart_glyph_id(this.address());
        }

        @NativeType(value="FT_UShort")
        public short end_glyph_id() {
            return FT_SvgGlyph.nend_glyph_id(this.address());
        }

        public FT_Matrix transform() {
            return FT_SvgGlyph.ntransform(this.address());
        }

        public FT_Vector delta() {
            return FT_SvgGlyph.ndelta(this.address());
        }
    }
}

