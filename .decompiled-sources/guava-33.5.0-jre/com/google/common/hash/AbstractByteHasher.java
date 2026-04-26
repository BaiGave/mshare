/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.common.hash.AbstractHasher;
import com.google.common.hash.Hasher;
import com.google.common.hash.Java8Compatibility;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jspecify.annotations.Nullable;

abstract class AbstractByteHasher
extends AbstractHasher {
    private @Nullable ByteBuffer scratch;

    AbstractByteHasher() {
    }

    protected abstract void update(byte var1);

    protected void update(byte[] b) {
        this.update(b, 0, b.length);
    }

    protected void update(byte[] b, int off, int len) {
        for (int i = off; i < off + len; ++i) {
            this.update(b[i]);
        }
    }

    protected void update(ByteBuffer b) {
        if (b.hasArray()) {
            this.update(b.array(), b.arrayOffset() + b.position(), b.remaining());
            Java8Compatibility.position(b, b.limit());
        } else {
            for (int remaining = b.remaining(); remaining > 0; --remaining) {
                this.update(b.get());
            }
        }
    }

    @CanIgnoreReturnValue
    private Hasher update(ByteBuffer scratch, int bytes) {
        try {
            this.update(scratch.array(), 0, bytes);
        }
        finally {
            Java8Compatibility.clear(scratch);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putByte(byte b) {
        this.update(b);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        this.update(bytes);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] bytes, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, bytes.length);
        this.update(bytes, off, len);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(ByteBuffer bytes) {
        this.update(bytes);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putShort(short s) {
        ByteBuffer scratch = this.scratch();
        scratch.putShort(s);
        return this.update(scratch, 2);
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putInt(int i) {
        ByteBuffer scratch = this.scratch();
        scratch.putInt(i);
        return this.update(scratch, 4);
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putLong(long l) {
        ByteBuffer scratch = this.scratch();
        scratch.putLong(l);
        return this.update(scratch, 8);
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putChar(char c) {
        ByteBuffer scratch = this.scratch();
        scratch.putChar(c);
        return this.update(scratch, 2);
    }

    private ByteBuffer scratch() {
        if (this.scratch == null) {
            this.scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        }
        return this.scratch;
    }
}

