/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public final class StringBuilderWriter
extends Writer {
    private final StringBuilder builder;
    private boolean closed = false;

    public StringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    public StringBuilderWriter(StringBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "'builder' cannot be null.");
    }

    @Override
    public void write(int c) throws IOException {
        this.ensureOpen();
        this.builder.append((char)c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        this.ensureOpen();
        this.builder.append(cbuf);
    }

    @Override
    public void write(String str) throws IOException {
        this.ensureOpen();
        this.builder.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        this.ensureOpen();
        this.builder.append(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        this.ensureOpen();
        this.builder.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        this.ensureOpen();
        this.builder.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        this.ensureOpen();
        this.builder.append(c);
        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        this.ensureOpen();
        this.builder.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.ensureOpen();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public String toString() {
        return this.builder.toString();
    }

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Writer has been closed.");
        }
    }
}

