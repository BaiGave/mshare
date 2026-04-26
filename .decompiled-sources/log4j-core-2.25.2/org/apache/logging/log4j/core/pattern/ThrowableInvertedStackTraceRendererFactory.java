/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ThrowableInvertedStackTraceRenderer;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRendererFactory;

final class ThrowableInvertedStackTraceRendererFactory
extends ThrowableStackTraceRendererFactory {
    static final ThrowableInvertedStackTraceRendererFactory INSTANCE = new ThrowableInvertedStackTraceRendererFactory();

    private ThrowableInvertedStackTraceRendererFactory() {
    }

    ThrowableInvertedStackTraceRenderer createStackTraceRenderer(ThrowableFormatOptions options) {
        return new ThrowableInvertedStackTraceRenderer(options.getIgnorePackages(), options.getLines());
    }
}

