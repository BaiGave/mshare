/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
import org.apache.commons.io.input.Input;

public abstract class UnsynchronizedReader
extends Reader {
    private static final int MAX_SKIP_BUFFER_SIZE = 8192;
    private boolean closed;
    private char[] skipBuffer;

    void checkOpen() throws IOException {
        Input.checkOpen(!this.isClosed());
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public long skip(long n) throws IOException {
        long remaining;
        int countOrEof;
        if (n < 0L) {
            throw new IllegalArgumentException("skip value < 0");
        }
        int bufSize = (int)Math.min(n, 8192L);
        if (this.skipBuffer == null || this.skipBuffer.length < bufSize) {
            this.skipBuffer = new char[bufSize];
        }
        for (remaining = n; remaining > 0L && (countOrEof = this.read(this.skipBuffer, 0, (int)Math.min(remaining, (long)bufSize))) != -1; remaining -= (long)countOrEof) {
        }
        return n - remaining;
    }
}

