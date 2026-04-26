/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.lzma;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

public class LZMACompressorOutputStream
extends CompressorOutputStream<LZMAOutputStream> {
    public static Builder builder() {
        return new Builder();
    }

    private LZMACompressorOutputStream(Builder builder) throws IOException {
        super(new LZMAOutputStream(builder.getOutputStream(), builder.lzma2Options, -1L));
    }

    public LZMACompressorOutputStream(OutputStream outputStream) throws IOException {
        this((Builder)LZMACompressorOutputStream.builder().setOutputStream(outputStream));
    }

    @Override
    public void finish() throws IOException {
        ((LZMAOutputStream)this.out()).finish();
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }

    public static class Builder
    extends AbstractStreamBuilder<LZMACompressorOutputStream, Builder> {
        private LZMA2Options lzma2Options = new LZMA2Options();

        @Override
        public LZMACompressorOutputStream get() throws IOException {
            return new LZMACompressorOutputStream(this);
        }

        public Builder setLzma2Options(LZMA2Options lzma2Options) {
            this.lzma2Options = lzma2Options != null ? lzma2Options : new LZMA2Options();
            return this;
        }
    }
}

