/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CompressFilterOutputStream<T extends OutputStream>
extends FilterOutputStream {
    private final AtomicBoolean closed = new AtomicBoolean();
    private boolean finished;

    private static byte[] write(OutputStream os, String data, Charset charset) throws IOException {
        byte[] bytes = data.getBytes(charset);
        os.write(bytes);
        return bytes;
    }

    public CompressFilterOutputStream() {
        super(null);
    }

    public CompressFilterOutputStream(T out) {
        super((OutputStream)out);
    }

    protected void checkOpen() throws IOException {
        if (this.isClosed()) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            super.close();
        }
    }

    public void finish() throws IOException {
        this.finished = true;
    }

    public boolean isClosed() {
        return this.closed.get();
    }

    protected boolean isFinished() {
        return this.finished;
    }

    protected T out() {
        return (T)this.out;
    }

    public long write(File file) throws IOException {
        return this.write(file.toPath());
    }

    public long write(Path path) throws IOException {
        return Files.copy(path, this);
    }

    public byte[] writeUsAscii(String data) throws IOException {
        return CompressFilterOutputStream.write(this, data, StandardCharsets.US_ASCII);
    }

    public byte[] writeUsAsciiRaw(String data) throws IOException {
        return CompressFilterOutputStream.write(this.out, data, StandardCharsets.US_ASCII);
    }

    public byte[] writeUtf8(String data) throws IOException {
        return CompressFilterOutputStream.write(this, data, StandardCharsets.UTF_8);
    }
}

