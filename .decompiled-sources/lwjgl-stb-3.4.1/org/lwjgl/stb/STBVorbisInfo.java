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

@NativeType(value="struct stb_vorbis_info")
public class STBVorbisInfo
extends Struct<STBVorbisInfo>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int SAMPLE_RATE;
    public static final int CHANNELS;
    public static final int SETUP_MEMORY_REQUIRED;
    public static final int SETUP_TEMP_MEMORY_REQUIRED;
    public static final int TEMP_MEMORY_REQUIRED;
    public static final int MAX_FRAME_SIZE;

    protected STBVorbisInfo(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected STBVorbisInfo create(long address, @Nullable ByteBuffer container) {
        return new STBVorbisInfo(address, container);
    }

    public STBVorbisInfo(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBVorbisInfo.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="unsigned int")
    public int sample_rate() {
        return STBVorbisInfo.nsample_rate(this.address());
    }

    public int channels() {
        return STBVorbisInfo.nchannels(this.address());
    }

    @NativeType(value="unsigned int")
    public int setup_memory_required() {
        return STBVorbisInfo.nsetup_memory_required(this.address());
    }

    @NativeType(value="unsigned int")
    public int setup_temp_memory_required() {
        return STBVorbisInfo.nsetup_temp_memory_required(this.address());
    }

    @NativeType(value="unsigned int")
    public int temp_memory_required() {
        return STBVorbisInfo.ntemp_memory_required(this.address());
    }

    public int max_frame_size() {
        return STBVorbisInfo.nmax_frame_size(this.address());
    }

    public static STBVorbisInfo malloc() {
        return new STBVorbisInfo(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static STBVorbisInfo calloc() {
        return new STBVorbisInfo(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static STBVorbisInfo create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new STBVorbisInfo(MemoryUtil.memAddress(container), container);
    }

    public static STBVorbisInfo create(long address) {
        return new STBVorbisInfo(address, null);
    }

    public static @Nullable STBVorbisInfo createSafe(long address) {
        return address == 0L ? null : new STBVorbisInfo(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(STBVorbisInfo.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBVorbisInfo.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static STBVorbisInfo malloc(MemoryStack stack) {
        return new STBVorbisInfo(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static STBVorbisInfo calloc(MemoryStack stack) {
        return new STBVorbisInfo(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nsample_rate(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SAMPLE_RATE);
    }

    public static int nchannels(long struct) {
        return MemoryUtil.memGetInt(struct + (long)CHANNELS);
    }

    public static int nsetup_memory_required(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SETUP_MEMORY_REQUIRED);
    }

    public static int nsetup_temp_memory_required(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SETUP_TEMP_MEMORY_REQUIRED);
    }

    public static int ntemp_memory_required(long struct) {
        return MemoryUtil.memGetInt(struct + (long)TEMP_MEMORY_REQUIRED);
    }

    public static int nmax_frame_size(long struct) {
        return MemoryUtil.memGetInt(struct + (long)MAX_FRAME_SIZE);
    }

    static {
        Struct.Layout layout = STBVorbisInfo.__struct(STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        SAMPLE_RATE = layout.offsetof(0);
        CHANNELS = layout.offsetof(1);
        SETUP_MEMORY_REQUIRED = layout.offsetof(2);
        SETUP_TEMP_MEMORY_REQUIRED = layout.offsetof(3);
        TEMP_MEMORY_REQUIRED = layout.offsetof(4);
        MAX_FRAME_SIZE = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<STBVorbisInfo, Buffer>
    implements NativeResource {
        private static final STBVorbisInfo ELEMENT_FACTORY = STBVorbisInfo.create(-1L);

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
        protected STBVorbisInfo getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="unsigned int")
        public int sample_rate() {
            return STBVorbisInfo.nsample_rate(this.address());
        }

        public int channels() {
            return STBVorbisInfo.nchannels(this.address());
        }

        @NativeType(value="unsigned int")
        public int setup_memory_required() {
            return STBVorbisInfo.nsetup_memory_required(this.address());
        }

        @NativeType(value="unsigned int")
        public int setup_temp_memory_required() {
            return STBVorbisInfo.nsetup_temp_memory_required(this.address());
        }

        @NativeType(value="unsigned int")
        public int temp_memory_required() {
            return STBVorbisInfo.ntemp_memory_required(this.address());
        }

        public int max_frame_size() {
            return STBVorbisInfo.nmax_frame_size(this.address());
        }
    }
}

