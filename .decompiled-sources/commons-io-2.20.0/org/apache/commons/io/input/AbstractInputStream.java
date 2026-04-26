/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.input.Input;

public abstract class AbstractInputStream
extends InputStream {
    private boolean closed;

    void checkOpen() throws IOException {
        Input.checkOpen(!this.isClosed());
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}

