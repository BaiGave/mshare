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
import org.lwjgl.util.freetype.FT_Var_Axis;
import org.lwjgl.util.freetype.FT_Var_Named_Style;

public class FT_MM_Var
extends Struct<FT_MM_Var> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NUM_AXIS;
    public static final int NUM_DESIGNS;
    public static final int NUM_NAMEDSTYLES;
    public static final int AXIS;
    public static final int NAMEDSTYLE;

    protected FT_MM_Var(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FT_MM_Var create(long address, @Nullable ByteBuffer container) {
        return new FT_MM_Var(address, container);
    }

    public FT_MM_Var(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FT_MM_Var.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="FT_UInt")
    public int num_axis() {
        return FT_MM_Var.nnum_axis(this.address());
    }

    @NativeType(value="FT_UInt")
    public int num_designs() {
        return FT_MM_Var.nnum_designs(this.address());
    }

    @NativeType(value="FT_UInt")
    public int num_namedstyles() {
        return FT_MM_Var.nnum_namedstyles(this.address());
    }

    @NativeType(value="FT_Var_Axis *")
    public FT_Var_Axis.Buffer axis() {
        return FT_MM_Var.naxis(this.address());
    }

    @NativeType(value="FT_Var_Named_Style *")
    public FT_Var_Named_Style.Buffer namedstyle() {
        return FT_MM_Var.nnamedstyle(this.address());
    }

    public static FT_MM_Var create(long address) {
        return new FT_MM_Var(address, null);
    }

    public static @Nullable FT_MM_Var createSafe(long address) {
        return address == 0L ? null : new FT_MM_Var(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static int nnum_axis(long struct) {
        return MemoryUtil.memGetInt(struct + (long)NUM_AXIS);
    }

    public static int nnum_designs(long struct) {
        return MemoryUtil.memGetInt(struct + (long)NUM_DESIGNS);
    }

    public static int nnum_namedstyles(long struct) {
        return MemoryUtil.memGetInt(struct + (long)NUM_NAMEDSTYLES);
    }

    public static FT_Var_Axis.Buffer naxis(long struct) {
        return FT_Var_Axis.create(MemoryUtil.memGetAddress(struct + (long)AXIS), FT_MM_Var.nnum_axis(struct));
    }

    public static FT_Var_Named_Style.Buffer nnamedstyle(long struct) {
        return FT_Var_Named_Style.create(MemoryUtil.memGetAddress(struct + (long)NAMEDSTYLE), FT_MM_Var.nnum_namedstyles(struct));
    }

    static {
        Struct.Layout layout = FT_MM_Var.__struct(FT_MM_Var.__member(4), FT_MM_Var.__member(4), FT_MM_Var.__member(4), FT_MM_Var.__member(POINTER_SIZE), FT_MM_Var.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NUM_AXIS = layout.offsetof(0);
        NUM_DESIGNS = layout.offsetof(1);
        NUM_NAMEDSTYLES = layout.offsetof(2);
        AXIS = layout.offsetof(3);
        NAMEDSTYLE = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<FT_MM_Var, Buffer> {
        private static final FT_MM_Var ELEMENT_FACTORY = FT_MM_Var.create(-1L);

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
        protected FT_MM_Var getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="FT_UInt")
        public int num_axis() {
            return FT_MM_Var.nnum_axis(this.address());
        }

        @NativeType(value="FT_UInt")
        public int num_designs() {
            return FT_MM_Var.nnum_designs(this.address());
        }

        @NativeType(value="FT_UInt")
        public int num_namedstyles() {
            return FT_MM_Var.nnum_namedstyles(this.address());
        }

        @NativeType(value="FT_Var_Axis *")
        public FT_Var_Axis.Buffer axis() {
            return FT_MM_Var.naxis(this.address());
        }

        @NativeType(value="FT_Var_Named_Style *")
        public FT_Var_Named_Style.Buffer namedstyle() {
            return FT_MM_Var.nnamedstyle(this.address());
        }
    }
}

