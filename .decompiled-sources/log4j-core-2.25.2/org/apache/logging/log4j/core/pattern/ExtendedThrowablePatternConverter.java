/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.ThrowableExtendedStackTraceRenderer;
import org.apache.logging.log4j.core.pattern.ThrowableExtendedStackTraceRendererFactory;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePropertyRendererFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Plugin(name="ExtendedThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"xEx", "xThrowable", "xException"})
public final class ExtendedThrowablePatternConverter
extends ThrowablePatternConverter {
    private ExtendedThrowablePatternConverter(@Nullable Configuration config, @Nullable String[] options) {
        super("ExtendedThrowable", "throwable", options, config, ThrowablePropertyRendererFactory.INSTANCE, ThrowableExtendedStackTraceRendererFactory.INSTANCE);
    }

    private static ThrowableExtendedStackTraceRenderer createRenderer(ThrowableFormatOptions options) {
        return new ThrowableExtendedStackTraceRenderer(options.getIgnorePackages(), options.getLines());
    }

    public static ExtendedThrowablePatternConverter newInstance(@Nullable Configuration config, @Nullable String[] options) {
        return new ExtendedThrowablePatternConverter(config, options);
    }
}

