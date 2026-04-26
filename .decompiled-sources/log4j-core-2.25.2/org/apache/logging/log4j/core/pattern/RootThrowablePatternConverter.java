/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.ThrowableInvertedPropertyRendererFactory;
import org.apache.logging.log4j.core.pattern.ThrowableInvertedStackTraceRendererFactory;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Plugin(name="RootThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"rEx", "rThrowable", "rException"})
public final class RootThrowablePatternConverter
extends ThrowablePatternConverter {
    private RootThrowablePatternConverter(@Nullable Configuration config, @Nullable String[] options) {
        super("RootThrowable", "throwable", options, config, ThrowableInvertedPropertyRendererFactory.INSTANCE, ThrowableInvertedStackTraceRendererFactory.INSTANCE);
    }

    public static RootThrowablePatternConverter newInstance(@Nullable Configuration config, @Nullable String[] options) {
        return new RootThrowablePatternConverter(config, options);
    }
}

