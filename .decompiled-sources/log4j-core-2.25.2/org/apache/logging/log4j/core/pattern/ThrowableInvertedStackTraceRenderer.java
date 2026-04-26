/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRenderer;
import org.jspecify.annotations.Nullable;

final class ThrowableInvertedStackTraceRenderer
extends ThrowableStackTraceRenderer<ThrowableStackTraceRenderer.Context> {
    private static final String WRAPPED_BY_CAPTION = "Wrapped by: ";

    ThrowableInvertedStackTraceRenderer(List<String> ignoredPackageNames, int maxLineCount) {
        super(ignoredPackageNames, maxLineCount);
    }

    @Override
    void renderThrowable(StringBuilder buffer, Throwable throwable, ThrowableStackTraceRenderer.Context context, Set<Throwable> visitedThrowables, String lineSeparator) {
        this.renderThrowable(buffer, throwable, context, visitedThrowables, "", lineSeparator, false);
    }

    private void renderThrowable(StringBuilder buffer, Throwable throwable, ThrowableStackTraceRenderer.Context context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator, boolean lineCapacityAcquired) {
        boolean circular;
        boolean bl = circular = !visitedThrowables.add(throwable);
        if (circular) {
            if (!lineCapacityAcquired) {
                this.acquireLineCapacity(context);
            }
            buffer.append("[CIRCULAR REFERENCE: ");
            ThrowableInvertedStackTraceRenderer.renderThrowableMessage(buffer, throwable);
            buffer.append(']');
            buffer.append(lineSeparator);
        } else {
            lineCapacityAcquired = this.renderCause(buffer, throwable.getCause(), context, visitedThrowables, prefix, lineSeparator, lineCapacityAcquired);
            if (!lineCapacityAcquired) {
                this.acquireLineCapacity(context);
            }
            ThrowableInvertedStackTraceRenderer.renderThrowableMessage(buffer, throwable);
            buffer.append(lineSeparator);
            this.renderStackTraceElements(buffer, throwable, context, prefix, lineSeparator);
            this.renderSuppressed(buffer, throwable.getSuppressed(), context, visitedThrowables, prefix + '\t', lineSeparator);
        }
    }

    private boolean renderCause(StringBuilder buffer, @Nullable Throwable cause, ThrowableStackTraceRenderer.Context context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator, boolean lineCapacityAcquired) {
        if (cause != null) {
            this.renderThrowable(buffer, cause, context, visitedThrowables, prefix, lineSeparator, lineCapacityAcquired);
            this.acquireLineCapacity(context);
            buffer.append(prefix);
            buffer.append(WRAPPED_BY_CAPTION);
            return true;
        }
        return lineCapacityAcquired;
    }

    @Override
    void renderSuppressed(StringBuilder buffer, Throwable[] suppressedThrowables, ThrowableStackTraceRenderer.Context context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator) {
        if (suppressedThrowables.length > 0) {
            this.acquireLineCapacity(context);
            buffer.append(prefix);
            buffer.append("Suppressed: ");
            for (int suppressedThrowableIndex = 0; suppressedThrowableIndex < suppressedThrowables.length; ++suppressedThrowableIndex) {
                Throwable suppressedThrowable = suppressedThrowables[suppressedThrowableIndex];
                boolean lineCapacityAcquired = suppressedThrowableIndex == 0;
                this.renderThrowable(buffer, suppressedThrowable, context, visitedThrowables, prefix, lineSeparator, lineCapacityAcquired);
            }
        }
    }
}

