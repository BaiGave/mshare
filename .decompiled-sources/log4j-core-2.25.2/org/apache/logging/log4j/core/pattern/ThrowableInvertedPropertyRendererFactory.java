/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.pattern.ThrowablePropertyRendererFactory;
import org.apache.logging.log4j.core.util.Throwables;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class ThrowableInvertedPropertyRendererFactory
extends ThrowablePropertyRendererFactory {
    static final ThrowableInvertedPropertyRendererFactory INSTANCE = new ThrowableInvertedPropertyRendererFactory();

    private ThrowableInvertedPropertyRendererFactory() {
        super(ThrowableInvertedPropertyRendererFactory::extractThrowingMethod);
    }

    private static @Nullable StackTraceElement extractThrowingMethod(Throwable throwable) {
        Throwable rootThrowable = Throwables.getRootCause(throwable);
        return rootThrowable.getStackTrace()[0];
    }
}

