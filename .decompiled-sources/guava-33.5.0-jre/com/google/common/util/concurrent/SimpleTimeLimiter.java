/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class SimpleTimeLimiter
implements TimeLimiter {
    private final ExecutorService executor;

    private SimpleTimeLimiter(ExecutorService executor) {
        this.executor = Preconditions.checkNotNull(executor);
    }

    public static SimpleTimeLimiter create(ExecutorService executor) {
        return new SimpleTimeLimiter(executor);
    }

    @Override
    public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(interfaceType);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Preconditions.checkArgument(interfaceType.isInterface(), "interfaceType must be an interface type");
        Set<Method> interruptibleMethods = SimpleTimeLimiter.findInterruptibleMethods(interfaceType);
        InvocationHandler handler = (obj, method, args) -> {
            Callable<@Nullable Object> callable = () -> {
                try {
                    return method.invoke(target, args);
                }
                catch (InvocationTargetException e) {
                    throw SimpleTimeLimiter.throwCause(e, false);
                }
            };
            return this.callWithTimeout(callable, timeoutDuration, timeoutUnit, interruptibleMethods.contains(method));
        };
        return SimpleTimeLimiter.newProxy(interfaceType, handler);
    }

    private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
        Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, handler);
        return interfaceType.cast(object);
    }

    @ParametricNullness
    private <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit, boolean amInterruptible) throws Exception {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Future<T> future = this.executor.submit(callable);
        try {
            return amInterruptible ? future.get(timeoutDuration, timeoutUnit) : Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
        }
        catch (InterruptedException e) {
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            throw SimpleTimeLimiter.throwCause(e, true);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            throw new UncheckedTimeoutException(e);
        }
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException, ExecutionException {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Future<T> future = this.executor.submit(callable);
        try {
            return future.get(timeoutDuration, timeoutUnit);
        }
        catch (InterruptedException | TimeoutException e) {
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            SimpleTimeLimiter.wrapAndThrowExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    @CanIgnoreReturnValue
    @ParametricNullness
    public <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, ExecutionException {
        Preconditions.checkNotNull(callable);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Future<T> future = this.executor.submit(callable);
        try {
            return Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            SimpleTimeLimiter.wrapAndThrowExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    public void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Future<?> future = this.executor.submit(runnable);
        try {
            future.get(timeoutDuration, timeoutUnit);
        }
        catch (InterruptedException | TimeoutException e) {
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            SimpleTimeLimiter.wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    public void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkNotNull(timeoutUnit);
        SimpleTimeLimiter.checkPositiveTimeout(timeoutDuration);
        Future<?> future = this.executor.submit(runnable);
        try {
            Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
        }
        catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
        catch (ExecutionException e) {
            SimpleTimeLimiter.wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    private static Exception throwCause(Exception e, boolean combineStackTraces) throws Exception {
        Throwable cause = e.getCause();
        if (cause == null) {
            throw e;
        }
        if (combineStackTraces) {
            StackTraceElement[] combined = ObjectArrays.concat(cause.getStackTrace(), e.getStackTrace(), StackTraceElement.class);
            cause.setStackTrace(combined);
        }
        if (cause instanceof Exception) {
            throw (Exception)cause;
        }
        if (cause instanceof Error) {
            throw (Error)cause;
        }
        throw e;
    }

    private static Set<Method> findInterruptibleMethods(Class<?> interfaceType) {
        HashSet<Method> set = new HashSet<Method>();
        for (Method m : interfaceType.getMethods()) {
            if (!SimpleTimeLimiter.declaresInterruptedEx(m)) continue;
            set.add(m);
        }
        return set;
    }

    private static boolean declaresInterruptedEx(Method method) {
        for (Class<?> exType : method.getExceptionTypes()) {
            if (exType != InterruptedException.class) continue;
            return true;
        }
        return false;
    }

    private static void wrapAndThrowExecutionExceptionOrError(Throwable cause) throws ExecutionException {
        if (cause instanceof Error) {
            throw new ExecutionError((Error)cause);
        }
        if (cause instanceof RuntimeException) {
            throw new UncheckedExecutionException(cause);
        }
        throw new ExecutionException(cause);
    }

    private static void wrapAndThrowRuntimeExecutionExceptionOrError(Throwable cause) {
        if (cause instanceof Error) {
            throw new ExecutionError((Error)cause);
        }
        throw new UncheckedExecutionException(cause);
    }

    private static void checkPositiveTimeout(long timeoutDuration) {
        Preconditions.checkArgument(timeoutDuration > 0L, "timeout must be positive: %s", timeoutDuration);
    }
}

