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
import org.lwjgl.util.freetype.FT_Face;

@NativeType(value="struct FT_CharMapRec")
public class FT_CharMap
extends Struct<FT_CharMap> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int FACE;
    public static final int ENCODING;
    public static final int PLATFORM_ID;
    public static final int ENCODING_ID;

    protected FT_CharMap(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_CharMap create(long address, @Nullable ByteBuffer container) {
        return new FT_CharMap(address, container);
    }

    public FT_CharMap(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_CharMap.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public FT_Face face() {
        return FT_CharMap.nface(this.address());
    }

    @NativeType(value="FT_Encoding")
    public int encoding() {
        return FT_CharMap.nencoding(this.address());
    }

    @NativeType(value="FT_UShort")
    public short platform_id() {
        return FT_CharMap.nplatform_id(this.address());
    }

    @NativeType(value="FT_UShort")
    public short encoding_id() {
        return FT_CharMap.nencoding_id(this.address());
    }

    public static FT_CharMap create(long address) {
        return new FT_CharMap(address, null);
    }

    public static @Nullable FT_CharMap createSafe(long address) {
        return address == 0L ? null : new FT_CharMap(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FT_Face nface(long struct) {
        return FT_Face.create(MemoryUtil.memGetAddress(struct + (long)FACE));
    }

    public static int nencoding(long struct) {
        return MemoryUtil.memGetInt(struct + (long)ENCODING);
    }

    public static short nplatform_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)PLATFORM_ID);
    }

    public static short nencoding_id(long struct) {
        return MemoryUtil.memGetShort(struct + (long)ENCODING_ID);
    }

    static {
        Struct.Layout layout = FT_CharMap.__struct(FT_CharMap.__member(POINTER_SIZE), FT_CharMap.__member(4), FT_CharMap.__member(2), FT_CharMap.__member(2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        FACE = layout.offsetof(0);
        ENCODING = layout.offsetof(1);
        PLATFORM_ID = layout.offsetof(2);
        ENCODING_ID = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<FT_CharMap, Buffer> {
        private static final FT_CharMap ELEMENT_FACTORY = FT_CharMap.create(-1L);

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
        protected FT_CharMap getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public FT_Face face() {
            return FT_CharMap.nface(this.address());
        }

        @NativeType(value="FT_Encoding")
        public int encoding() {
            return FT_CharMap.nencoding(this.address());
        }

        @NativeType(value="FT_UShort")
        public short platform_id() {
            return FT_CharMap.nplatform_id(this.address());
        }

        @NativeType(value="FT_UShort")
        public short encoding_id() {
            return FT_CharMap.nencoding_id(this.address());
        }
    }
}

