/*
 * Decompiled with CFR 0.152.
 */
package net.jpountz.lz4;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
import net.jpountz.util.SafeUtils;
import net.jpountz.xxhash.XXHashFactory;

public class LZ4BlockInputStream
extends FilterInputStream {
    private final LZ4FastDecompressor fastDecompressor;
    private final LZ4SafeDecompressor safeDecompressor;
    private final Checksum checksum;
    private final boolean stopOnEmptyBlock;
    private byte[] buffer;
    private byte[] compressedBuffer;
    private int originalLen;
    private int o;
    private boolean finished;

    @Deprecated
    public LZ4BlockInputStream(InputStream in, LZ4FastDecompressor fastDecompressor, Checksum checksum, boolean stopOnEmptyBlock) {
        this(in, fastDecompressor, null, checksum, stopOnEmptyBlock);
    }

    @Deprecated
    public LZ4BlockInputStream(InputStream in, LZ4FastDecompressor fastDecompressor, Checksum checksum) {
        this(in, fastDecompressor, checksum, true);
    }

    @Deprecated
    public LZ4BlockInputStream(InputStream in, LZ4FastDecompressor fastDecompressor) {
        this(in, fastDecompressor, XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum(), true);
    }

    @Deprecated
    public LZ4BlockInputStream(InputStream in, boolean stopOnEmptyBlock) {
        this(in, LZ4Factory.fastestInstance().fastDecompressor(), XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum(), stopOnEmptyBlock);
    }

    @Deprecated
    public LZ4BlockInputStream(InputStream in) {
        this(in, LZ4Factory.fastestInstance().fastDecompressor());
    }

    private LZ4BlockInputStream(InputStream in, LZ4FastDecompressor fastDecompressor, LZ4SafeDecompressor safeDecompressor, Checksum checksum, boolean stopOnEmptyBlock) {
        super(in);
        this.fastDecompressor = fastDecompressor;
        this.safeDecompressor = safeDecompressor;
        this.checksum = checksum;
        this.stopOnEmptyBlock = stopOnEmptyBlock;
        this.buffer = new byte[0];
        this.compressedBuffer = new byte[LZ4BlockOutputStream.HEADER_LENGTH];
        this.originalLen = 0;
        this.o = 0;
        this.finished = false;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public int available() throws IOException {
        return this.originalLen - this.o;
    }

    @Override
    public int read() throws IOException {
        if (this.finished) {
            return -1;
        }
        if (this.o == this.originalLen) {
            this.refill();
        }
        if (this.finished) {
            return -1;
        }
        return this.buffer[this.o++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        SafeUtils.checkRange(b, off, len);
        if (this.finished) {
            return -1;
        }
        if (this.o == this.originalLen) {
            this.refill();
        }
        if (this.finished) {
            return -1;
        }
        len = Math.min(len, this.originalLen - this.o);
        System.arraycopy(this.buffer, this.o, b, off, len);
        this.o += len;
        return len;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0L || this.finished) {
            return 0L;
        }
        if (this.o == this.originalLen) {
            this.refill();
        }
        if (this.finished) {
            return 0L;
        }
        int skipped = (int)Math.min(n, (long)(this.originalLen - this.o));
        this.o += skipped;
        return skipped;
    }

    private void refill() throws IOException {
        if (!this.tryReadFully(this.compressedBuffer, LZ4BlockOutputStream.HEADER_LENGTH)) {
            if (this.stopOnEmptyBlock) {
                throw new EOFException("Stream ended prematurely");
            }
            this.finished = true;
            return;
        }
        for (int i = 0; i < LZ4BlockOutputStream.MAGIC_LENGTH; ++i) {
            if (this.compressedBuffer[i] == LZ4BlockOutputStream.MAGIC[i]) continue;
            throw new IOException("Stream is corrupted");
        }
        int token = this.compressedBuffer[LZ4BlockOutputStream.MAGIC_LENGTH] & 0xFF;
        int compressionMethod = token & 0xF0;
        int compressionLevel = 10 + (token & 0xF);
        if (compressionMethod != 16 && compressionMethod != 32) {
            throw new IOException("Stream is corrupted");
        }
        int compressedLen = SafeUtils.readIntLE(this.compressedBuffer, LZ4BlockOutputStream.MAGIC_LENGTH + 1);
        this.originalLen = SafeUtils.readIntLE(this.compressedBuffer, LZ4BlockOutputStream.MAGIC_LENGTH + 5);
        int check = SafeUtils.readIntLE(this.compressedBuffer, LZ4BlockOutputStream.MAGIC_LENGTH + 9);
        assert (LZ4BlockOutputStream.HEADER_LENGTH == LZ4BlockOutputStream.MAGIC_LENGTH + 13);
        if (this.originalLen > 1 << compressionLevel || this.originalLen < 0 || compressedLen < 0 || this.originalLen == 0 && compressedLen != 0 || this.originalLen != 0 && compressedLen == 0 || compressionMethod == 16 && this.originalLen != compressedLen) {
            throw new IOException("Stream is corrupted");
        }
        if (this.originalLen == 0 && compressedLen == 0) {
            if (check != 0) {
                throw new IOException("Stream is corrupted");
            }
            if (!this.stopOnEmptyBlock) {
                this.refill();
            } else {
                this.finished = true;
            }
            return;
        }
        if (this.buffer.length < this.originalLen) {
            this.buffer = new byte[Math.max(this.originalLen, this.buffer.length * 3 / 2)];
        }
        switch (compressionMethod) {
            case 16: {
                this.readFully(this.buffer, this.originalLen);
                break;
            }
            case 32: {
                if (this.compressedBuffer.length < compressedLen) {
                    this.compressedBuffer = new byte[Math.max(compressedLen, this.compressedBuffer.length * 3 / 2)];
                }
                this.readFully(this.compressedBuffer, compressedLen);
                try {
                    int compressedLen2;
                    int decompressedLen;
                    if (this.fastDecompressor == null ? (decompressedLen = this.safeDecompressor.decompress(this.compressedBuffer, 0, compressedLen, this.buffer, 0, this.originalLen)) != this.originalLen : compressedLen != (compressedLen2 = this.fastDecompressor.decompress(this.compressedBuffer, 0, this.buffer, 0, this.originalLen))) {
                        throw new IOException("Stream is corrupted");
                    }
                    break;
                }
                catch (LZ4Exception e) {
                    throw new IOException("Stream is corrupted", e);
                }
            }
            default: {
                throw new AssertionError();
            }
        }
        this.checksum.reset();
        this.checksum.update(this.buffer, 0, this.originalLen);
        if ((int)this.checksum.getValue() != check) {
            throw new IOException("Stream is corrupted");
        }
        this.o = 0;
    }

    private boolean tryReadFully(byte[] b, int len) throws IOException {
        int read;
        int r;
        for (read = 0; read < len; read += r) {
            r = this.in.read(b, read, len - read);
            if (r >= 0) continue;
            return false;
        }
        assert (len == read);
        return true;
    }

    private void readFully(byte[] b, int len) throws IOException {
        if (!this.tryReadFully(b, len)) {
            throw new EOFException("Stream ended prematurely");
        }
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public String toString() {
        return this.getClass().getSimpleName() + "(in=" + this.in + ", decompressor=" + (this.fastDecompressor != null ? this.fastDecompressor : this.safeDecompressor) + ", checksum=" + this.checksum + ")";
    }

    public static final class Builder {
        private boolean stopOnEmptyBlock = true;
        private LZ4FastDecompressor fastDecompressor;
        private LZ4SafeDecompressor safeDecompressor;
        private Checksum checksum;

        private Builder() {
        }

        public Builder withStopOnEmptyBlock(boolean stopOnEmptyBlock) {
            this.stopOnEmptyBlock = stopOnEmptyBlock;
            return this;
        }

        public Builder withDecompressor(LZ4FastDecompressor fastDecompressor) {
            this.fastDecompressor = fastDecompressor;
            this.safeDecompressor = null;
            return this;
        }

        public Builder withDecompressor(LZ4SafeDecompressor safeDecompressor) {
            this.safeDecompressor = safeDecompressor;
            this.fastDecompressor = null;
            return this;
        }

        public Builder withChecksum(Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        public LZ4BlockInputStream build(InputStream in) {
            Checksum checksum = this.checksum;
            LZ4FastDecompressor fastDecompressor = this.fastDecompressor;
            LZ4SafeDecompressor safeDecompressor = this.safeDecompressor;
            if (checksum == null) {
                checksum = XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum();
            }
            if (fastDecompressor == null && safeDecompressor == null) {
                safeDecompressor = LZ4Factory.fastestInstance().safeDecompressor();
            }
            return new LZ4BlockInputStream(in, fastDecompressor, safeDecompressor, checksum, this.stopOnEmptyBlock);
        }
    }
}

