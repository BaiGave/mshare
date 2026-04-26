/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.input.AbstractInputStream;

public class CircularInputStream
extends AbstractInputStream {
    private long byteCount;
    private int position = -1;
    private final byte[] repeatedContent;
    private final long targetByteCount;

    private static byte[] validate(byte[] repeatContent) {
        Objects.requireNonNull(repeatContent, "repeatContent");
        for (byte b : repeatContent) {
            if (b != -1) continue;
            throw new IllegalArgumentException("repeatContent contains the end-of-stream marker -1");
        }
        return repeatContent;
    }

    public CircularInputStream(byte[] repeatContent, long targetByteCount) {
        this.repeatedContent = CircularInputStream.validate(repeatContent);
        if (repeatContent.length == 0) {
            throw new IllegalArgumentException("repeatContent is empty.");
        }
        this.targetByteCount = targetByteCount;
    }

    @Override
    public int available() throws IOException {
        return this.isClosed() ? 0 : (this.targetByteCount <= Integer.MAX_VALUE ? Math.max(Integer.MAX_VALUE, (int)this.targetByteCount) : Integer.MAX_VALUE);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.byteCount = this.targetByteCount;
    }

    @Override
    public int read() {
        if (this.targetByteCount >= 0L || this.isClosed()) {
            if (this.byteCount == this.targetByteCount) {
                return -1;
            }
            ++this.byteCount;
        }
        this.position = (this.position + 1) % this.repeatedContent.length;
        return this.repeatedContent[this.position] & 0xFF;
    }
}

