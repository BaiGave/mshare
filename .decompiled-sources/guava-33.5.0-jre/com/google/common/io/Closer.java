/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class Closer
implements Closeable {
    @VisibleForTesting
    final Suppressor suppressor;
    private final Deque<Closeable> stack = new ArrayDeque<Closeable>(4);
    private @Nullable Throwable thrown;
    private static final Suppressor SUPPRESSING_SUPPRESSOR = (closeable, thrown, suppressed) -> {
        if (thrown == suppressed) {
            return;
        }
        try {
            thrown.addSuppressed(suppressed);
        }
        catch (Throwable e) {
            Closeables.logger.log(Level.WARNING, "Suppressing exception thrown when closing " + closeable, suppressed);
        }
    };

    public static Closer create() {
        return new Closer(SUPPRESSING_SUPPRESSOR);
    }

    @VisibleForTesting
    Closer(Suppressor suppressor) {
        this.suppressor = Preconditions.checkNotNull(suppressor);
    }

    @CanIgnoreReturnValue
    @ParametricNullness
    public <C extends Closeable> C register(@ParametricNullness C closeable) {
        if (closeable != null) {
            this.stack.addFirst(closeable);
        }
        return closeable;
    }

    public RuntimeException rethrow(Throwable e) throws IOException {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    public <X extends Exception> RuntimeException rethrow(Throwable e, Class<X> declaredType) throws IOException, X {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfInstanceOf(e, declaredType);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    public <X1 extends Exception, X2 extends Exception> RuntimeException rethrow(Throwable e, Class<X1> declaredType1, Class<X2> declaredType2) throws IOException, X1, X2 {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfInstanceOf(e, declaredType1);
        Throwables.throwIfInstanceOf(e, declaredType2);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    @Override
    public void close() throws IOException {
        Throwable throwable = this.thrown;
        while (!this.stack.isEmpty()) {
            Closeable closeable = this.stack.removeFirst();
            try {
                closeable.close();
            }
            catch (Throwable e) {
                if (throwable == null) {
                    throwable = e;
                    continue;
                }
                this.suppressor.suppress(closeable, throwable, e);
            }
        }
        if (this.thrown == null && throwable != null) {
            Throwables.throwIfInstanceOf(throwable, IOException.class);
            Throwables.throwIfUnchecked(throwable);
            throw new AssertionError((Object)throwable);
        }
    }

    @VisibleForTesting
    static interface Suppressor {
        public void suppress(Closeable var1, Throwable var2, Throwable var3);
    }
}

