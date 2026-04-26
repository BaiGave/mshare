/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.pattern.ClassResourceInfo;
import org.apache.logging.log4j.core.pattern.ThrowableStackTraceRenderer;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;

final class ThrowableExtendedStackTraceRenderer
extends ThrowableStackTraceRenderer<ExtendedContext> {
    private static final ClassLoadingStrategy[] CLASS_LOADING_STRATEGIES = new ClassLoadingStrategy[]{(loader, className) -> loader != null ? loader.loadClass(className) : null, (loader, className) -> LoaderUtil.loadClass(className), (loader, className) -> ThrowableExtendedStackTraceRenderer.class.getClassLoader().loadClass(className)};

    ThrowableExtendedStackTraceRenderer(List<String> ignoredPackageNames, int maxLineCount) {
        super(ignoredPackageNames, maxLineCount);
    }

    @Override
    ExtendedContext createContext(Throwable throwable) {
        return ExtendedContext.ofThrowable(throwable);
    }

    @Override
    void renderStackTraceElement(StringBuilder buffer, StackTraceElement stackTraceElement, ExtendedContext context, String prefix, String lineSeparator) {
        boolean stackTraceElementIgnored = this.isStackTraceElementIgnored(stackTraceElement);
        if (stackTraceElementIgnored) {
            ++context.ignoredStackTraceElementCount;
            return;
        }
        if (context.ignoredStackTraceElementCount > 0) {
            this.renderSuppressedCount(buffer, context, prefix, lineSeparator);
            context.ignoredStackTraceElementCount = 0;
        }
        this.acquireLineCapacity(context);
        buffer.append(prefix);
        buffer.append("\tat ");
        buffer.append(stackTraceElement);
        ClassResourceInfo classResourceInfo = (ClassResourceInfo)context.classResourceInfoByName.get(stackTraceElement.getClassName());
        if (classResourceInfo != null) {
            buffer.append(' ');
            classResourceInfo.render(buffer);
        }
        buffer.append(lineSeparator);
    }

    private static interface ClassLoadingStrategy {
        public Class<?> run(ClassLoader var1, String var2) throws Exception;
    }

    static final class ExtendedContext
    extends ThrowableStackTraceRenderer.Context {
        private final Map<String, ClassResourceInfo> classResourceInfoByName;

        private ExtendedContext(int ignoredStackTraceElementCount, Map<Throwable, ThrowableStackTraceRenderer.Context.Metadata> metadataByThrowable, Map<String, ClassResourceInfo> classResourceInfoByName) {
            super(ignoredStackTraceElementCount, metadataByThrowable);
            this.classResourceInfoByName = classResourceInfoByName;
        }

        private static ExtendedContext ofThrowable(Throwable throwable) {
            Map<Throwable, ThrowableStackTraceRenderer.Context.Metadata> metadataByThrowable = ThrowableStackTraceRenderer.Context.Metadata.ofThrowable(throwable);
            Map<String, ClassResourceInfo> classResourceInfoByName = ExtendedContext.createClassResourceInfoByName(throwable, metadataByThrowable);
            return new ExtendedContext(0, metadataByThrowable, classResourceInfoByName);
        }

        private static Map<String, ClassResourceInfo> createClassResourceInfoByName(Throwable rootThrowable, Map<Throwable, ThrowableStackTraceRenderer.Context.Metadata> metadataByThrowable) {
            Throwable throwable;
            Deque<Class<?>> executionStackTrace = StackLocatorUtil.getCurrentStackTrace();
            HashMap<String, ClassResourceInfo> classResourceInfoByName = new HashMap<String, ClassResourceInfo>();
            HashSet<Throwable> visitedThrowables = new HashSet<Throwable>();
            ArrayDeque<Throwable> pendingThrowables = new ArrayDeque<Throwable>(Collections.singleton(rootThrowable));
            while ((throwable = (Throwable)pendingThrowables.poll()) != null && visitedThrowables.add(throwable)) {
                ThrowableStackTraceRenderer.Context.Metadata metadata;
                Throwable cause = throwable.getCause();
                if (cause != null) {
                    pendingThrowables.offer(cause);
                }
                if ((metadata = metadataByThrowable.get(throwable)) == null) continue;
                Class<?> executionStackTraceElementClass = executionStackTrace.isEmpty() ? null : executionStackTrace.peekLast();
                ClassLoader lastLoader = null;
                StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                for (int throwableStackIndex = metadata.stackLength - 1; throwableStackIndex >= 0; --throwableStackIndex) {
                    StackTraceElement throwableStackTraceElement = stackTraceElements[throwableStackIndex];
                    String throwableStackTraceElementClassName = throwableStackTraceElement.getClassName();
                    ClassResourceInfo classResourceInfo = (ClassResourceInfo)classResourceInfoByName.get(throwableStackTraceElementClassName);
                    if (classResourceInfo != null) {
                        if (classResourceInfo.clazz == null) continue;
                        lastLoader = classResourceInfo.clazz.getClassLoader();
                        continue;
                    }
                    if (executionStackTraceElementClass != null && throwableStackTraceElementClassName.equals(executionStackTraceElementClass.getName())) {
                        classResourceInfo = new ClassResourceInfo(executionStackTraceElementClass, true);
                        classResourceInfoByName.put(throwableStackTraceElementClassName, classResourceInfo);
                        lastLoader = classResourceInfo.clazz.getClassLoader();
                        executionStackTrace.pollLast();
                        executionStackTraceElementClass = executionStackTrace.peekLast();
                        continue;
                    }
                    Class<?> stackTraceElementClass = ExtendedContext.loadClass(lastLoader, throwableStackTraceElementClassName);
                    classResourceInfo = stackTraceElementClass != null ? new ClassResourceInfo(stackTraceElementClass, false) : ClassResourceInfo.UNKNOWN;
                    classResourceInfoByName.put(throwableStackTraceElementClassName, classResourceInfo);
                }
            }
            return classResourceInfoByName;
        }

        private static Class<?> loadClass(ClassLoader loader, String className) {
            for (ClassLoadingStrategy strategy : CLASS_LOADING_STRATEGIES) {
                try {
                    Class<?> clazz = strategy.run(loader, className);
                    if (clazz == null) continue;
                    return clazz;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return null;
        }
    }
}

