/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

@J2ktIncompatible
@GwtIncompatible
public abstract class ByteSink {
    protected ByteSink() {
    }

    public CharSink asCharSink(Charset charset) {
        return new AsCharSink(charset);
    }

    public abstract OutputStream openStream() throws IOException;

    public OutputStream openBufferedStream() throws IOException {
        OutputStream out = this.openStream();
        return out instanceof BufferedOutputStream ? (BufferedOutputStream)out : new BufferedOutputStream(out);
    }

    public void write(byte[] bytes) throws IOException {
        Preconditions.checkNotNull(bytes);
        try (OutputStream out = this.openStream();){
            out.write(bytes);
        }
    }

    @CanIgnoreReturnValue
    public long writeFrom(InputStream input) throws IOException {
        Preconditions.checkNotNull(input);
        try (OutputStream out = this.openStream();){
            long l = ByteStreams.copy(input, out);
            return l;
        }
    }

    private final class AsCharSink
    extends CharSink {
        private final Charset charset;

        private AsCharSink(Charset charset) {
            this.charset = Preconditions.checkNotNull(charset);
        }

        @Override
        public Writer openStream() throws IOException {
            return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
        }

        public String toString() {
            return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
        }
    }
}

