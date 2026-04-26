/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.core.pattern.ThrowableRenderer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ThrowableStackTraceRenderer<C extends Context>
implements ThrowableRenderer {
    private static final RuntimeException MAX_LINE_COUNT_EXCEEDED = new RuntimeException("max-line-count-exceeded");
    private static final String CAUSED_BY_CAPTION = "Caused by: ";
    static final String SUPPRESSED_CAPTION = "Suppressed: ";
    final List<String> ignoredPackageNames;
    final int maxLineCount;

    ThrowableStackTraceRenderer(List<String> ignoredPackageNames, int maxLineCount) {
        this.ignoredPackageNames = ignoredPackageNames;
        this.maxLineCount = maxLineCount;
    }

    @Override
    public final void renderThrowable(StringBuilder buffer, Throwable throwable, String lineSeparator) {
        block3: {
            if (this.maxLineCount > 0) {
                try {
                    C context = this.createContext(throwable);
                    this.renderThrowable(buffer, throwable, context, new HashSet<Throwable>(), lineSeparator);
                }
                catch (Exception error) {
                    if (error == MAX_LINE_COUNT_EXCEEDED) break block3;
                    throw error;
                }
            }
        }
    }

    C createContext(Throwable throwable) {
        Map<Throwable, Context.Metadata> metadataByThrowable = Context.Metadata.ofThrowable(throwable);
        return (C)new Context(0, metadataByThrowable);
    }

    void renderThrowable(StringBuilder buffer, Throwable throwable, C context, Set<Throwable> visitedThrowables, String lineSeparator) {
        this.renderThrowable(buffer, throwable, context, visitedThrowables, "", lineSeparator, "");
    }

    private void renderThrowable(StringBuilder buffer, Throwable throwable, C context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator, String caption) {
        this.acquireLineCapacity(context);
        boolean circular = !visitedThrowables.add(throwable);
        buffer.append(prefix);
        buffer.append(caption);
        if (circular) {
            buffer.append("[CIRCULAR REFERENCE: ");
            ThrowableStackTraceRenderer.renderThrowableMessage(buffer, throwable);
            buffer.append(']');
            buffer.append(lineSeparator);
        } else {
            ThrowableStackTraceRenderer.renderThrowableMessage(buffer, throwable);
            buffer.append(lineSeparator);
            this.renderStackTraceElements(buffer, throwable, context, prefix, lineSeparator);
            this.renderSuppressed(buffer, throwable.getSuppressed(), context, visitedThrowables, prefix + '\t', lineSeparator);
            this.renderCause(buffer, throwable.getCause(), context, visitedThrowables, prefix, lineSeparator);
        }
    }

    void acquireLineCapacity(C context) {
        if (((Context)context).lineCount < this.maxLineCount) {
            ++((Context)context).lineCount;
        } else {
            throw MAX_LINE_COUNT_EXCEEDED;
        }
    }

    void renderSuppressed(StringBuilder buffer, Throwable[] suppressedThrowables, C context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator) {
        for (Throwable suppressedThrowable : suppressedThrowables) {
            this.renderThrowable(buffer, suppressedThrowable, context, visitedThrowables, prefix, lineSeparator, SUPPRESSED_CAPTION);
        }
    }

    private void renderCause(StringBuilder buffer, @Nullable Throwable cause, C context, Set<Throwable> visitedThrowables, String prefix, String lineSeparator) {
        if (cause != null) {
            this.renderThrowable(buffer, cause, context, visitedThrowables, prefix, lineSeparator, CAUSED_BY_CAPTION);
        }
    }

    static void renderThrowableMessage(StringBuilder buffer, Throwable throwable) {
        String message = throwable.getLocalizedMessage();
        buffer.append(throwable.getClass().getName());
        if (message != null) {
            buffer.append(": ");
            buffer.append(message);
        }
    }

    final void renderStackTraceElements(StringBuilder buffer, Throwable throwable, C context, String prefix, String lineSeparator) {
        ((Context)context).ignoredStackTraceElementCount = 0;
        Context.Metadata metadata = ((Context)context).metadataByThrowable.get(throwable);
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (int i = 0; i < metadata.stackLength; ++i) {
            this.renderStackTraceElement(buffer, stackTraceElements[i], context, prefix, lineSeparator);
        }
        if (((Context)context).ignoredStackTraceElementCount > 0) {
            this.renderSuppressedCount(buffer, context, prefix, lineSeparator);
        }
        if (metadata.commonElementCount != 0) {
            this.acquireLineCapacity(context);
            buffer.append(prefix);
            buffer.append("\t... ");
            buffer.append(metadata.commonElementCount);
            buffer.append(" more");
            buffer.append(lineSeparator);
        }
    }

    void renderStackTraceElement(StringBuilder buffer, StackTraceElement stackTraceElement, C context, String prefix, String lineSeparator) {
        boolean stackTraceElementIgnored = this.isStackTraceElementIgnored(stackTraceElement);
        if (stackTraceElementIgnored) {
            ++((Context)context).ignoredStackTraceElementCount;
            return;
        }
        if (((Context)context).ignoredStackTraceElementCount > 0) {
            this.renderSuppressedCount(buffer, context, prefix, lineSeparator);
            ((Context)context).ignoredStackTraceElementCount = 0;
        }
        this.acquireLineCapacity(context);
        buffer.append(prefix);
        buffer.append("\tat ");
        buffer.append(stackTraceElement);
        buffer.append(lineSeparator);
    }

    boolean isStackTraceElementIgnored(StackTraceElement element) {
        if (this.ignoredPackageNames != null) {
            String className = element.getClassName();
            for (String ignoredPackageName : this.ignoredPackageNames) {
                if (!className.startsWith(ignoredPackageName)) continue;
                return true;
            }
        }
        return false;
    }

    void renderSuppressedCount(StringBuilder buffer, C context, String prefix, String lineSeparator) {
        this.acquireLineCapacity(context);
        buffer.append(prefix);
        if (((Context)context).ignoredStackTraceElementCount == 1) {
            buffer.append("\t...");
        } else {
            buffer.append("\t... suppressed ");
            buffer.append(((Context)context).ignoredStackTraceElementCount);
            buffer.append(" lines");
        }
        buffer.append(lineSeparator);
    }

    static class Context {
        int lineCount = 0;
        int ignoredStackTraceElementCount;
        final Map<Throwable, Metadata> metadataByThrowable;

        Context(int ignoredStackTraceElementCount, Map<Throwable, Metadata> metadataByThrowable) {
            this.ignoredStackTraceElementCount = ignoredStackTraceElementCount;
            this.metadataByThrowable = metadataByThrowable;
        }

        static final class Metadata {
            final int commonElementCount;
            final int stackLength;

            private Metadata(int commonElementCount, int stackLength) {
                this.commonElementCount = commonElementCount;
                this.stackLength = stackLength;
            }

            static Map<Throwable, Metadata> ofThrowable(Throwable throwable) {
                HashMap<Throwable, Metadata> metadataByThrowable = new HashMap<Throwable, Metadata>();
                Metadata.populateMetadata(metadataByThrowable, new HashSet<Throwable>(), null, throwable);
                return metadataByThrowable;
            }

            private static void populateMetadata(Map<Throwable, Metadata> metadataByThrowable, Set<Throwable> visitedThrowables, @Nullable Throwable parentThrowable, Throwable throwable) {
                @Nullable StackTraceElement[] rootTrace = parentThrowable == null ? null : parentThrowable.getStackTrace();
                Metadata metadata = Metadata.populateMetadata(rootTrace, throwable.getStackTrace());
                metadataByThrowable.put(throwable, metadata);
                for (Throwable suppressed : throwable.getSuppressed()) {
                    if (visitedThrowables.contains(suppressed)) continue;
                    visitedThrowables.add(suppressed);
                    Metadata.populateMetadata(metadataByThrowable, visitedThrowables, throwable, suppressed);
                }
                @Nullable Throwable cause = throwable.getCause();
                if (cause != null && !visitedThrowables.contains(cause)) {
                    visitedThrowables.add(cause);
                    Metadata.populateMetadata(metadataByThrowable, visitedThrowables, throwable, cause);
                }
            }

            private static Metadata populateMetadata(@Nullable StackTraceElement[] parentTrace, StackTraceElement[] currentTrace) {
                int stackLength;
                int commonElementCount;
                if (parentTrace != null) {
                    int currentIndex;
                    int parentIndex = parentTrace.length - 1;
                    for (currentIndex = currentTrace.length - 1; parentIndex >= 0 && currentIndex >= 0 && parentTrace[parentIndex].equals(currentTrace[currentIndex]); --parentIndex, --currentIndex) {
                    }
                    commonElementCount = currentTrace.length - 1 - currentIndex;
                    stackLength = currentIndex + 1;
                } else {
                    commonElementCount = 0;
                    stackLength = currentTrace.length;
                }
                return new Metadata(commonElementCount, stackLength);
            }
        }
    }
}

