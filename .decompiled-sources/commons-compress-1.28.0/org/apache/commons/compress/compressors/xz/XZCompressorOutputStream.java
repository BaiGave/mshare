/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.xz;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class XZCompressorOutputStream
extends CompressorOutputStream<XZOutputStream> {
    public static Builder builder() {
        return new Builder();
    }

    private XZCompressorOutputStream(Builder builder) throws IOException {
        super(new XZOutputStream(builder.getOutputStream(), (FilterOptions)builder.lzma2Options));
    }

    public XZCompressorOutputStream(OutputStream outputStream) throws IOException {
        this((Builder)XZCompressorOutputStream.builder().setOutputStream(outputStream));
    }

    @Deprecated
    public XZCompressorOutputStream(OutputStream outputStream, int preset) throws IOException {
        super(new XZOutputStream(outputStream, (FilterOptions)new LZMA2Options(preset)));
    }

    @Override
    public void finish() throws IOException {
        ((XZOutputStream)this.out()).finish();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }

    public static class Builder
    extends AbstractStreamBuilder<XZCompressorOutputStream, Builder> {
        private LZMA2Options lzma2Options = new LZMA2Options();

        @Override
        public XZCompressorOutputStream get() throws IOException {
            return new XZCompressorOutputStream(this);
        }

        public Builder setLzma2Options(LZMA2Options lzma2Options) {
            this.lzma2Options = lzma2Options != null ? lzma2Options : new LZMA2Options();
            return this;
        }
    }
}

