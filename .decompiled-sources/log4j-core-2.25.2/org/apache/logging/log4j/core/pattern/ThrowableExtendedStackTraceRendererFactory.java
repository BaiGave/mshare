/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ThrowableExtendedStackTraceRenderer;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRendererFactory;

final class ThrowableExtendedStackTraceRendererFactory
extends ThrowableStackTraceRendererFactory {
    static final ThrowableExtendedStackTraceRendererFactory INSTANCE = new ThrowableExtendedStackTraceRendererFactory();

    private ThrowableExtendedStackTraceRendererFactory() {
    }

    ThrowableExtendedStackTraceRenderer createStackTraceRenderer(ThrowableFormatOptions options) {
        return new ThrowableExtendedStackTraceRenderer(options.getIgnorePackages(), options.getLines());
    }
}

