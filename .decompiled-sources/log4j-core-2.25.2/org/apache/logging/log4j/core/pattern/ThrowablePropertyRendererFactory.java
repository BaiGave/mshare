/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.logging.log4j.core.pattern.ThrowableRenderer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ThrowablePropertyRendererFactory {
    private static final ThrowableRenderer MESSAGE_RENDERER = (buffer, throwable, lineSeparator) -> {
        String message = throwable.getMessage();
        buffer.append(message);
    };
    private static final ThrowableRenderer LOCALIZED_MESSAGE_RENDERER = (buffer, throwable, lineSeparator) -> {
        String localizedMessage = throwable.getLocalizedMessage();
        buffer.append(localizedMessage);
    };
    private static final Function<Throwable, @Nullable StackTraceElement> THROWING_METHOD_EXTRACTOR = throwable -> {
        @Nullable StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        return stackTraceElements != null && stackTraceElements.length > 0 ? stackTraceElements[0] : null;
    };
    static final ThrowablePropertyRendererFactory INSTANCE = new ThrowablePropertyRendererFactory(THROWING_METHOD_EXTRACTOR);
    private final Map<String, ThrowableRenderer> rendererByPropertyName;

    ThrowablePropertyRendererFactory(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        this.rendererByPropertyName = ThrowablePropertyRendererFactory.createRendererByPropertyName(throwingMethodExtractor);
    }

    private static Map<String, ThrowableRenderer> createRendererByPropertyName(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        HashMap<String, ThrowableRenderer> map = new HashMap<String, ThrowableRenderer>();
        map.put("short.message", MESSAGE_RENDERER);
        map.put("short.localizedMessage", LOCALIZED_MESSAGE_RENDERER);
        map.put("short.className", ThrowablePropertyRendererFactory.createClassNameRenderer(throwingMethodExtractor));
        map.put("short.methodName", ThrowablePropertyRendererFactory.createMethodNameRenderer(throwingMethodExtractor));
        map.put("short.lineNumber", ThrowablePropertyRendererFactory.createLineNumberRenderer(throwingMethodExtractor));
        map.put("short.fileName", ThrowablePropertyRendererFactory.createFileNameRenderer(throwingMethodExtractor));
        return map;
    }

    private static ThrowableRenderer createClassNameRenderer(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        return (buffer, throwable, lineSeparator) -> {
            @Nullable StackTraceElement throwingMethod = (StackTraceElement)throwingMethodExtractor.apply(throwable);
            if (throwingMethod != null) {
                String className = throwingMethod.getClassName();
                buffer.append(className);
            }
        };
    }

    private static ThrowableRenderer createMethodNameRenderer(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        return (buffer, throwable, lineSeparator) -> {
            @Nullable StackTraceElement throwingMethod = (StackTraceElement)throwingMethodExtractor.apply(throwable);
            if (throwingMethod != null) {
                String methodName = throwingMethod.getMethodName();
                buffer.append(methodName);
            }
        };
    }

    private static ThrowableRenderer createLineNumberRenderer(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        return (buffer, throwable, lineSeparator) -> {
            @Nullable StackTraceElement throwingMethod = (StackTraceElement)throwingMethodExtractor.apply(throwable);
            if (throwingMethod != null) {
                int lineNumber = throwingMethod.getLineNumber();
                buffer.append(lineNumber);
            }
        };
    }

    private static ThrowableRenderer createFileNameRenderer(Function<Throwable, @Nullable StackTraceElement> throwingMethodExtractor) {
        return (buffer, throwable, lineSeparator) -> {
            @Nullable StackTraceElement throwingMethod = (StackTraceElement)throwingMethodExtractor.apply(throwable);
            if (throwingMethod != null) {
                String fileName = throwingMethod.getFileName();
                buffer.append(fileName);
            }
        };
    }

    final @Nullable ThrowableRenderer createPropertyRenderer(@Nullable String[] options) {
        if (options != null && options.length > 0) {
            String propertyName = options[0];
            return this.rendererByPropertyName.get(propertyName);
        }
        return null;
    }
}

