/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.pattern.ThrowablePropertyRendererFactory;
import org.apache.logging.log4j.core.pattern.ThrowableRenderer;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRendererFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Plugin(name="ThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"ex", "throwable", "exception"})
public class ThrowablePatternConverter
extends LogEventPatternConverter {
    @Deprecated
    protected final List<PatternFormatter> formatters;
    private final Function<LogEvent, String> effectiveLineSeparatorProvider;
    protected final ThrowableFormatOptions options;
    private final ThrowableRenderer renderer;

    @Deprecated
    protected ThrowablePatternConverter(String name, String style, @Nullable String[] options) {
        this(name, style, options, null, null, null);
    }

    @Deprecated
    protected ThrowablePatternConverter(String name, String style, @Nullable String[] options, @Nullable Configuration config) {
        this(name, style, options, config, null, null);
    }

    ThrowablePatternConverter(String name, String style, @Nullable String[] options, @Nullable Configuration config, @Nullable ThrowablePropertyRendererFactory propertyRendererFactory, @Nullable ThrowableStackTraceRendererFactory stackTraceRendererFactory) {
        super(name, style);
        this.options = ThrowableFormatOptions.newInstance(options);
        ArrayList<PatternFormatter> suffixFormatters = new ArrayList<PatternFormatter>();
        this.effectiveLineSeparatorProvider = ThrowablePatternConverter.createEffectiveLineSeparator(this.options.getSeparator(), this.options.getSuffix(), config, suffixFormatters);
        this.formatters = Collections.unmodifiableList(suffixFormatters);
        this.renderer = ThrowablePatternConverter.createEffectiveRenderer(options, this.options, propertyRendererFactory, stackTraceRendererFactory);
    }

    public static ThrowablePatternConverter newInstance(@Nullable Configuration config, @Nullable String[] options) {
        return new ThrowablePatternConverter("Throwable", "throwable", options, config, null, null);
    }

    @Override
    public void format(LogEvent event, StringBuilder buffer) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(buffer, "buffer");
        Throwable throwable = event.getThrown();
        if (throwable != null) {
            String lineSeparator = this.effectiveLineSeparatorProvider.apply(event);
            this.renderer.renderThrowable(buffer, throwable, lineSeparator);
        }
    }

    @Override
    public boolean handlesThrowable() {
        return true;
    }

    public ThrowableFormatOptions getOptions() {
        return this.options;
    }

    private static Function<LogEvent, String> createEffectiveLineSeparator(String separator, @Nullable String suffix, @Nullable Configuration config, List<PatternFormatter> suffixFormatters) {
        Objects.requireNonNull(separator, "separator");
        Objects.requireNonNull(suffixFormatters, "suffixFormatters");
        if (suffix != null) {
            PatternParser parser = PatternLayout.createPatternParser(config);
            List<PatternFormatter> parsedSuffixFormatters = parser.parse(suffix);
            for (PatternFormatter suffixFormatter : parsedSuffixFormatters) {
                if (suffixFormatter.handlesThrowable()) continue;
                suffixFormatters.add(suffixFormatter);
            }
            return logEvent -> {
                boolean blankSuffix;
                StringBuilder buffer = new StringBuilder();
                buffer.append(' ');
                for (PatternFormatter suffixFormatter : suffixFormatters) {
                    suffixFormatter.format((LogEvent)logEvent, buffer);
                }
                boolean bl = blankSuffix = buffer.length() == 1;
                if (blankSuffix) {
                    return separator;
                }
                buffer.append(separator);
                return buffer.toString();
            };
        }
        return logEvent -> separator;
    }

    private static ThrowableRenderer createEffectiveRenderer(String[] rawOptions, ThrowableFormatOptions options, @Nullable ThrowablePropertyRendererFactory propertyRendererFactory, @Nullable ThrowableStackTraceRendererFactory stackTraceRendererFactory) {
        ThrowablePropertyRendererFactory effectivePropertyRendererFactory = propertyRendererFactory != null ? propertyRendererFactory : ThrowablePropertyRendererFactory.INSTANCE;
        ThrowableRenderer propertyRenderer = effectivePropertyRendererFactory.createPropertyRenderer(rawOptions);
        if (propertyRenderer != null) {
            return propertyRenderer;
        }
        ThrowableStackTraceRendererFactory effectiveStackTraceRendererFactory = stackTraceRendererFactory != null ? stackTraceRendererFactory : ThrowableStackTraceRendererFactory.INSTANCE;
        return effectiveStackTraceRendererFactory.createStackTraceRenderer(options);
    }

    @Deprecated
    protected String getSuffix(LogEvent logEvent) {
        Objects.requireNonNull(logEvent, "logEvent");
        String effectiveLineSeparator = this.effectiveLineSeparatorProvider.apply(logEvent);
        if (this.options.getSeparator().equals(effectiveLineSeparator)) {
            return "";
        }
        return effectiveLineSeparator.substring(1, effectiveLineSeparator.length() - this.options.getSeparator().length());
    }
}

