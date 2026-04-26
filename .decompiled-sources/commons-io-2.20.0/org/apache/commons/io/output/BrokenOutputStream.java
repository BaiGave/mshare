/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.io.function.Erase;

public class BrokenOutputStream
extends OutputStream {
    public static final BrokenOutputStream INSTANCE = new BrokenOutputStream();
    private final Function<String, Throwable> exceptionFunction;

    public BrokenOutputStream() {
        this((String m) -> new IOException("Broken output stream: " + m));
    }

    public BrokenOutputStream(Function<String, Throwable> exceptionFunction) {
        this.exceptionFunction = exceptionFunction;
    }

    @Deprecated
    public BrokenOutputStream(IOException exception) {
        this((String m) -> exception);
    }

    @Deprecated
    public BrokenOutputStream(Supplier<Throwable> exceptionSupplier) {
        this.exceptionFunction = m -> (Throwable)exceptionSupplier.get();
    }

    public BrokenOutputStream(Throwable exception) {
        this((String m) -> exception);
    }

    @Override
    public void close() throws IOException {
        throw this.rethrow("close()");
    }

    @Override
    public void flush() throws IOException {
        throw this.rethrow("flush()");
    }

    private RuntimeException rethrow(String method) {
        return Erase.rethrow(this.exceptionFunction.apply(method));
    }

    @Override
    public void write(int b) throws IOException {
        throw this.rethrow("write(int)");
    }
}

