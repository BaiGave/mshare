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

public class FT_PaintColrGlyph
extends Struct<FT_PaintColrGlyph> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int GLYPHID;

    protected FT_PaintColrGlyph(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_PaintColrGlyph create(long address, @Nullable ByteBuffer container) {
        return new FT_PaintColrGlyph(address, container);
    }

    public FT_PaintColrGlyph(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_PaintColrGlyph.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_UInt")
    public int glyphID() {
        return FT_PaintColrGlyph.nglyphID(this.address());
    }

    public static FT_PaintColrGlyph create(long address) {
        return new FT_PaintColrGlyph(address, null);
    }

    public static @Nullable FT_PaintColrGlyph createSafe(long address) {
        return address == 0L ? null : new FT_PaintColrGlyph(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static int nglyphID(long struct) {
        return MemoryUtil.memGetInt(struct + (long)GLYPHID);
    }

    static {
        Struct.Layout layout = FT_PaintColrGlyph.__struct(FT_PaintColrGlyph.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        GLYPHID = layout.offsetof(0);
    }

    public static class Buffer
    extends StructBuffer<FT_PaintColrGlyph, Buffer> {
        private static final FT_PaintColrGlyph ELEMENT_FACTORY = FT_PaintColrGlyph.create(-1L);

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
        protected FT_PaintColrGlyph getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_UInt")
        public int glyphID() {
            return FT_PaintColrGlyph.nglyphID(this.address());
        }
    }
}

