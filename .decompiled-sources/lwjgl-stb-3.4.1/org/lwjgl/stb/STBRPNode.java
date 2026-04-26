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

@NativeType(value="struct stbrp_node")
public class STBRPNode
extends Struct<STBRPNode>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X;
    public static final int Y;
    public static final int NEXT;

    protected STBRPNode(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected STBRPNode create(long address, @Nullable ByteBuffer container) {
        return new STBRPNode(address, container);
    }

    public STBRPNode(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBRPNode.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="stbrp_coord")
    public int x() {
        return STBRPNode.nx(this.address());
    }

    @NativeType(value="stbrp_coord")
    public int y() {
        return STBRPNode.ny(this.address());
    }

    @NativeType(value="stbrp_node *")
    public @Nullable STBRPNode next() {
        return STBRPNode.nnext(this.address());
    }

    public static STBRPNode malloc() {
        return new STBRPNode(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static STBRPNode calloc() {
        return new STBRPNode(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static STBRPNode create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new STBRPNode(MemoryUtil.memAddress(container), container);
    }

    public static STBRPNode create(long address) {
        return new STBRPNode(address, null);
    }

    public static @Nullable STBRPNode createSafe(long address) {
        return address == 0L ? null : new STBRPNode(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(STBRPNode.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBRPNode.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static STBRPNode malloc(MemoryStack stack) {
        return new STBRPNode(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static STBRPNode calloc(MemoryStack stack) {
        return new STBRPNode(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nx(long struct) {
        return MemoryUtil.memGetInt(struct + (long)X);
    }

    public static int ny(long struct) {
        return MemoryUtil.memGetInt(struct + (long)Y);
    }

    public static @Nullable STBRPNode nnext(long struct) {
        return STBRPNode.createSafe(MemoryUtil.memGetAddress(struct + (long)NEXT));
    }

    static {
        Struct.Layout layout = STBRPNode.__struct(STBRPNode.__member(4), STBRPNode.__member(4), STBRPNode.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        NEXT = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<STBRPNode, Buffer>
    implements NativeResource {
        private static final STBRPNode ELEMENT_FACTORY = STBRPNode.create(-1L);

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
        protected STBRPNode getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="stbrp_coord")
        public int x() {
            return STBRPNode.nx(this.address());
        }

        @NativeType(value="stbrp_coord")
        public int y() {
            return STBRPNode.ny(this.address());
        }

        @NativeType(value="stbrp_node *")
        public @Nullable STBRPNode next() {
            return STBRPNode.nnext(this.address());
        }
    }
}

