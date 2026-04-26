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

public class FT_Prop_IncreaseXHeight
extends Struct<FT_Prop_IncreaseXHeight> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int FACE;
    public static final int LIMIT;

    protected FT_Prop_IncreaseXHeight(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_Prop_IncreaseXHeight create(long address, @Nullable ByteBuffer container) {
        return new FT_Prop_IncreaseXHeight(address, container);
    }

    public FT_Prop_IncreaseXHeight(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_Prop_IncreaseXHeight.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public FT_Face face() {
        return FT_Prop_IncreaseXHeight.nface(this.address());
    }

    @NativeType(value="FT_UInt")
    public int limit$() {
        return FT_Prop_IncreaseXHeight.nlimit$(this.address());
    }

    public static FT_Prop_IncreaseXHeight create(long address) {
        return new FT_Prop_IncreaseXHeight(address, null);
    }

    public static @Nullable FT_Prop_IncreaseXHeight createSafe(long address) {
        return address == 0L ? null : new FT_Prop_IncreaseXHeight(address, null);
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

    public static int nlimit$(long struct) {
        return MemoryUtil.memGetInt(struct + (long)LIMIT);
    }

    static {
        Struct.Layout layout = FT_Prop_IncreaseXHeight.__struct(FT_Prop_IncreaseXHeight.__member(POINTER_SIZE), FT_Prop_IncreaseXHeight.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        FACE = layout.offsetof(0);
        LIMIT = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<FT_Prop_IncreaseXHeight, Buffer> {
        private static final FT_Prop_IncreaseXHeight ELEMENT_FACTORY = FT_Prop_IncreaseXHeight.create(-1L);

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
        protected FT_Prop_IncreaseXHeight getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public FT_Face face() {
            return FT_Prop_IncreaseXHeight.nface(this.address());
        }

        @NativeType(value="FT_UInt")
        public int limit$() {
            return FT_Prop_IncreaseXHeight.nlimit$(this.address());
        }
    }
}

