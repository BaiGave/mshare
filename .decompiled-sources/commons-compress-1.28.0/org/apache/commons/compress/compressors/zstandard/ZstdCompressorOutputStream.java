/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.zstandard;

import com.github.luben.zstd.ZstdOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdConstants;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.lang3.ArrayUtils;

public class ZstdCompressorOutputStream
extends CompressorOutputStream<ZstdOutputStream> {
    public static Builder builder() {
        return new Builder();
    }

    private static ZstdOutputStream toZstdOutputStream(Builder builder) throws IOException {
        OutputStream outputStream = builder.getOutputStream();
        if (outputStream instanceof ZstdOutputStream) {
            return (ZstdOutputStream)outputStream;
        }
        return new ZstdOutputStream(outputStream).setChainLog(builder.chainLog).setChecksum(builder.checksum).setCloseFrameOnFlush(builder.closeFrameOnFlush).setDict(builder.dict != null ? builder.dict : ArrayUtils.EMPTY_BYTE_ARRAY).setHashLog(builder.hashLog).setJobSize(builder.jobSize).setLevel(builder.level).setMinMatch(builder.minMatch).setOverlapLog(builder.overlapLog).setSearchLog(builder.searchLog).setStrategy(builder.strategy).setTargetLength(builder.targetLength).setWindowLog(builder.windowLog).setWorkers(builder.workers);
    }

    private ZstdCompressorOutputStream(Builder builder) throws IOException {
        super(ZstdCompressorOutputStream.toZstdOutputStream(builder));
    }

    public ZstdCompressorOutputStream(OutputStream outStream) throws IOException {
        this((Builder)ZstdCompressorOutputStream.builder().setOutputStream(outStream));
    }

    @Deprecated
    public ZstdCompressorOutputStream(OutputStream outStream, int level) throws IOException {
        this(((Builder)ZstdCompressorOutputStream.builder().setOutputStream(outStream)).setLevel(level));
    }

    @Deprecated
    public ZstdCompressorOutputStream(OutputStream outStream, int level, boolean closeFrameOnFlush) throws IOException {
        this(((Builder)ZstdCompressorOutputStream.builder().setOutputStream(outStream)).setLevel(level).setCloseFrameOnFlush(closeFrameOnFlush));
    }

    @Deprecated
    public ZstdCompressorOutputStream(OutputStream outStream, int level, boolean closeFrameOnFlush, boolean checksum) throws IOException {
        this(((Builder)ZstdCompressorOutputStream.builder().setOutputStream(outStream)).setLevel(level).setCloseFrameOnFlush(closeFrameOnFlush).setChecksum(checksum));
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }

    public static final class Builder
    extends AbstractStreamBuilder<ZstdCompressorOutputStream, Builder> {
        private int chainLog;
        private boolean checksum;
        private boolean closeFrameOnFlush;
        private byte[] dict;
        private int hashLog;
        private int jobSize;
        private int level = ZstdConstants.ZSTD_CLEVEL_DEFAULT;
        private int minMatch;
        private int overlapLog;
        private int searchLog;
        private int strategy;
        private int targetLength;
        private int windowLog;
        private int workers;

        @Override
        public ZstdCompressorOutputStream get() throws IOException {
            return new ZstdCompressorOutputStream(this);
        }

        public Builder setChainLog(int chainLog) {
            this.chainLog = chainLog;
            return this;
        }

        public Builder setChecksum(boolean checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder setCloseFrameOnFlush(boolean closeFrameOnFlush) {
            this.closeFrameOnFlush = closeFrameOnFlush;
            return this;
        }

        public Builder setDict(byte[] dict) {
            this.dict = dict;
            return this;
        }

        public Builder setHashLog(int hashLog) {
            this.hashLog = hashLog;
            return this;
        }

        public Builder setJobSize(int jobSize) {
            this.jobSize = jobSize;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setMinMatch(int minMatch) {
            this.minMatch = minMatch;
            return this;
        }

        public Builder setOverlapLog(int overlapLog) {
            this.overlapLog = overlapLog;
            return this;
        }

        public Builder setSearchLog(int searchLog) {
            this.searchLog = searchLog;
            return this;
        }

        public Builder setStrategy(int strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder setTargetLength(int targetLength) {
            this.targetLength = targetLength;
            return this;
        }

        public Builder setWindowLog(int windowLog) {
            this.windowLog = windowLog;
            return this;
        }

        public Builder setWorkers(int workers) {
            this.workers = workers;
            return this;
        }
    }
}

