/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRenderer;

class ThrowableStackTraceRendererFactory {
    static final ThrowableStackTraceRendererFactory INSTANCE = new ThrowableStackTraceRendererFactory();

    ThrowableStackTraceRendererFactory() {
    }

    ThrowableStackTraceRenderer<?> createStackTraceRenderer(ThrowableFormatOptions options) {
        return new ThrowableStackTraceRenderer(options.getIgnorePackages(), options.getLines());
    }
}

