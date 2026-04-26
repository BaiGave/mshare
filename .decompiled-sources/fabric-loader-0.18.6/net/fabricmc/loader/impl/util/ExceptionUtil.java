/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.UncheckedIOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import net.fabricmc.loader.impl.util.SystemProperties;

public final class ExceptionUtil {
    private static final boolean THROW_DIRECTLY = SystemProperties.isSet("fabric.debug.throwDirectly");

    public static <T extends Throwable> T gatherExceptions(Throwable exc, T prev, Function<Throwable, T> mainExcFactory) throws T {
        exc = ExceptionUtil.unwrap(exc);
        if (THROW_DIRECTLY) {
            throw (Throwable)mainExcFactory.apply(exc);
        }
        if (prev == null) {
            return (T)((Throwable)mainExcFactory.apply(exc));
        }
        if (exc != prev) {
            for (Throwable t : prev.getSuppressed()) {
                if (!exc.equals(t)) continue;
                return prev;
            }
            prev.addSuppressed(exc);
        }
        return prev;
    }

    public static RuntimeException wrap(Throwable exc) {
        if (exc instanceof RuntimeException) {
            return (RuntimeException)exc;
        }
        if ((exc = ExceptionUtil.unwrap(exc)) instanceof RuntimeException) {
            return (RuntimeException)exc;
        }
        return new WrappedException(exc);
    }

    private static Throwable unwrap(Throwable exc) {
        Throwable ret;
        if ((exc instanceof WrappedException || exc instanceof UncheckedIOException || exc instanceof ExecutionException || exc instanceof CompletionException) && (ret = exc.getCause()) != null) {
            return ExceptionUtil.unwrap(ret);
        }
        return exc;
    }

    public static final class WrappedException
    extends RuntimeException {
        public WrappedException(Throwable cause) {
            super(cause);
        }
    }
}

