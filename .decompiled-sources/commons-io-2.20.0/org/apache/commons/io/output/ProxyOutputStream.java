/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.build.AbstractStreamBuilder;

public class ProxyOutputStream
extends FilterOutputStream {
    ProxyOutputStream(Builder builder) throws IOException {
        super(builder.getOutputStream());
    }

    public ProxyOutputStream(OutputStream delegate) {
        super(delegate);
    }

    protected void afterWrite(int n) throws IOException {
    }

    protected void beforeWrite(int n) throws IOException {
    }

    @Override
    public void close() throws IOException {
        IOUtils.close((Closeable)this.out, this::handleIOException);
    }

    @Override
    public void flush() throws IOException {
        try {
            this.out.flush();
        }
        catch (IOException e) {
            this.handleIOException(e);
        }
    }

    protected void handleIOException(IOException e) throws IOException {
        throw e;
    }

    public ProxyOutputStream setReference(OutputStream out) {
        this.out = out;
        return this;
    }

    OutputStream unwrap() {
        return this.out;
    }

    @Override
    public void write(byte[] bts) throws IOException {
        try {
            int len = IOUtils.length(bts);
            this.beforeWrite(len);
            this.out.write(bts);
            this.afterWrite(len);
        }
        catch (IOException e) {
            this.handleIOException(e);
        }
    }

    @Override
    public void write(byte[] bts, int st, int end) throws IOException {
        try {
            this.beforeWrite(end);
            this.out.write(bts, st, end);
            this.afterWrite(end);
        }
        catch (IOException e) {
            this.handleIOException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            this.beforeWrite(1);
            this.out.write(b);
            this.afterWrite(1);
        }
        catch (IOException e) {
            this.handleIOException(e);
        }
    }

    public static class Builder
    extends AbstractStreamBuilder<ProxyOutputStream, Builder> {
        @Override
        public ProxyOutputStream get() throws IOException {
            return new ProxyOutputStream(this);
        }
    }
}

