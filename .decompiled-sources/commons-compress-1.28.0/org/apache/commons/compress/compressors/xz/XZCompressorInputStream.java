/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.xz;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.MemoryLimitException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.BoundedInputStream;
import org.tukaani.xz.SingleXZInputStream;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

public class XZCompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private final BoundedInputStream countingStream;
    private final InputStream in;

    public static Builder builder() {
        return new Builder();
    }

    public static boolean matches(byte[] signature, int length) {
        if (length < XZ.HEADER_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < XZ.HEADER_MAGIC.length; ++i) {
            if (signature[i] == XZ.HEADER_MAGIC[i]) continue;
            return false;
        }
        return true;
    }

    private XZCompressorInputStream(Builder builder) throws IOException {
        this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(builder.getInputStream())).get();
        this.in = builder.decompressConcatenated ? new XZInputStream((InputStream)this.countingStream, builder.memoryLimitKiB) : new SingleXZInputStream((InputStream)this.countingStream, builder.memoryLimitKiB);
    }

    public XZCompressorInputStream(InputStream inputStream) throws IOException {
        this((Builder)XZCompressorInputStream.builder().setInputStream(inputStream));
    }

    @Deprecated
    public XZCompressorInputStream(InputStream inputStream, boolean decompressConcatenated) throws IOException {
        this(((Builder)XZCompressorInputStream.builder().setInputStream(inputStream)).setDecompressConcatenated(decompressConcatenated));
    }

    @Deprecated
    public XZCompressorInputStream(InputStream inputStream, boolean decompressConcatenated, int memoryLimitKiB) throws IOException {
        this(((Builder)XZCompressorInputStream.builder().setInputStream(inputStream)).setDecompressConcatenated(decompressConcatenated).setMemoryLimitKiB(memoryLimitKiB));
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    @Override
    public long getCompressedCount() {
        return this.countingStream.getCount();
    }

    @Override
    public int read() throws IOException {
        try {
            int ret = this.in.read();
            this.count(ret == -1 ? -1 : 1);
            return ret;
        }
        catch (org.tukaani.xz.MemoryLimitException e) {
            throw new MemoryLimitException((long)e.getMemoryNeeded(), e.getMemoryLimit(), e);
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        try {
            int ret = this.in.read(buf, off, len);
            this.count(ret);
            return ret;
        }
        catch (org.tukaani.xz.MemoryLimitException e) {
            throw new MemoryLimitException((long)e.getMemoryNeeded(), e.getMemoryLimit(), e);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return IOUtils.skip(this.in, n);
        }
        catch (org.tukaani.xz.MemoryLimitException e) {
            throw new MemoryLimitException((long)e.getMemoryNeeded(), e.getMemoryLimit(), e);
        }
    }

    public static class Builder
    extends AbstractStreamBuilder<XZCompressorInputStream, Builder> {
        private int memoryLimitKiB = -1;
        private boolean decompressConcatenated;

        @Override
        public XZCompressorInputStream get() throws IOException {
            return new XZCompressorInputStream(this);
        }

        public Builder setDecompressConcatenated(boolean decompressConcatenated) {
            this.decompressConcatenated = decompressConcatenated;
            return this;
        }

        public Builder setMemoryLimitKiB(int memoryLimitKiB) {
            this.memoryLimitKiB = memoryLimitKiB;
            return this;
        }
    }
}

