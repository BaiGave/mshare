/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.DirectExecutor;
import com.google.common.util.concurrent.DirectExecutorService;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.Internal;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.SequentialExecutor;
import com.google.common.util.concurrent.SneakyThrows;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.TrustedListenableFutureTask;
import com.google.common.util.concurrent.WrappingExecutorService;
import com.google.common.util.concurrent.WrappingScheduledExecutorService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class MoreExecutors {
    private MoreExecutors() {
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, Duration terminationTimeout) {
        return MoreExecutors.getExitingExecutorService(executor, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
        return new Application().getExitingExecutorService(executor, terminationTimeout, timeUnit);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ExecutorService getExitingExecutorService(ThreadPoolExecutor executor) {
        return new Application().getExitingExecutorService(executor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, Duration terminationTimeout) {
        return MoreExecutors.getExitingScheduledExecutorService(executor, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
        return new Application().getExitingScheduledExecutorService(executor, terminationTimeout, timeUnit);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor) {
        return new Application().getExitingScheduledExecutorService(executor);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static void addDelayedShutdownHook(ExecutorService service, Duration terminationTimeout) {
        MoreExecutors.addDelayedShutdownHook(service, Internal.toNanosSaturated(terminationTimeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static void addDelayedShutdownHook(ExecutorService service, long terminationTimeout, TimeUnit timeUnit) {
        new Application().addDelayedShutdownHook(service, terminationTimeout, timeUnit);
    }

    @J2ktIncompatible
    @GwtIncompatible
    private static void useDaemonThreadFactory(ThreadPoolExecutor executor) {
        executor.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true).setThreadFactory(executor.getThreadFactory()).build());
    }

    @GwtIncompatible
    public static ListeningExecutorService newDirectExecutorService() {
        return new DirectExecutorService();
    }

    public static Executor directExecutor() {
        return DirectExecutor.INSTANCE;
    }

    @GwtIncompatible
    public static Executor newSequentialExecutor(Executor delegate) {
        return new SequentialExecutor(delegate);
    }

    @GwtIncompatible
    public static ListeningExecutorService listeningDecorator(ExecutorService delegate) {
        return delegate instanceof ListeningExecutorService ? (ListeningExecutorService)delegate : (delegate instanceof ScheduledExecutorService ? new ScheduledListeningDecorator((ScheduledExecutorService)delegate) : new ListeningDecorator(delegate));
    }

    @GwtIncompatible
    public static ListeningScheduledExecutorService listeningDecorator(ScheduledExecutorService delegate) {
        return delegate instanceof ListeningScheduledExecutorService ? (ListeningScheduledExecutorService)delegate : new ScheduledListeningDecorator(delegate);
    }

    @J2ktIncompatible
    @GwtIncompatible
    @ParametricNullness
    static <T> T invokeAnyImpl(ListeningExecutorService executorService, Collection<? extends Callable<T>> tasks, boolean timed, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
        return MoreExecutors.invokeAnyImpl(executorService, tasks, timed, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    @J2ktIncompatible
    @GwtIncompatible
    @ParametricNullness
    static <T> T invokeAnyImpl(ListeningExecutorService executorService, Collection<? extends Callable<T>> tasks, boolean timed, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutionException ee;
        ArrayList<ListenableFuture<T>> futures;
        block16: {
            Preconditions.checkNotNull(executorService);
            Preconditions.checkNotNull(unit);
            int ntasks = tasks.size();
            Preconditions.checkArgument(ntasks > 0);
            futures = Lists.newArrayListWithCapacity(ntasks);
            LinkedBlockingQueue<Future<T>> futureQueue = Queues.newLinkedBlockingQueue();
            long timeoutNanos = unit.toNanos(timeout);
            ee = null;
            long lastTime = timed ? System.nanoTime() : 0L;
            Iterator<Callable<T>> it = tasks.iterator();
            futures.add(MoreExecutors.submitAndAddQueueListener(executorService, it.next(), futureQueue));
            --ntasks;
            int active = 1;
            while (true) {
                Object now22;
                Future f;
                if ((f = (Future)futureQueue.poll()) == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(MoreExecutors.submitAndAddQueueListener(executorService, it.next(), futureQueue));
                        ++active;
                    } else {
                        if (active == 0) break;
                        if (timed) {
                            f = (Future)futureQueue.poll(timeoutNanos, TimeUnit.NANOSECONDS);
                            if (f == null) {
                                throw new TimeoutException();
                            }
                            long now22 = System.nanoTime();
                            timeoutNanos -= now22 - lastTime;
                            lastTime = now22;
                        } else {
                            f = (Future)futureQueue.take();
                        }
                    }
                }
                if (f == null) continue;
                --active;
                try {
                    now22 = f.get();
                }
                catch (ExecutionException eex) {
                    ee = eex;
                    continue;
                }
                catch (InterruptedException iex) {
                    throw iex;
                }
                catch (Exception rex) {
                    ee = new ExecutionException(rex);
                    continue;
                }
                return (T)now22;
                break;
            }
            if (ee != null) break block16;
            ee = new ExecutionException(null);
        }
        throw ee;
        finally {
            for (Future future : futures) {
                future.cancel(true);
            }
        }
    }

    @J2ktIncompatible
    @GwtIncompatible
    private static <T> ListenableFuture<T> submitAndAddQueueListener(ListeningExecutorService executorService, Callable<T> task, BlockingQueue<Future<T>> queue) {
        Future future = executorService.submit((Callable)task);
        future.addListener(() -> MoreExecutors.lambda$submitAndAddQueueListener$0(queue, (ListenableFuture)future), MoreExecutors.directExecutor());
        return future;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static ThreadFactory platformThreadFactory() {
        if (!MoreExecutors.isAppEngineWithApiClasses()) {
            return Executors.defaultThreadFactory();
        }
        try {
            return (ThreadFactory)Class.forName("com.google.appengine.api.ThreadManager").getMethod("currentRequestThreadFactory", new Class[0]).invoke(null, new Object[0]);
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Couldn't invoke ThreadManager.currentRequestThreadFactory", e);
        }
        catch (InvocationTargetException e) {
            throw SneakyThrows.sneakyThrow(e.getCause());
        }
    }

    @J2ktIncompatible
    @GwtIncompatible
    private static boolean isAppEngineWithApiClasses() {
        if (System.getProperty("com.google.appengine.runtime.environment") == null) {
            return false;
        }
        try {
            Class.forName("com.google.appengine.api.utils.SystemProperty");
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        try {
            return Class.forName("com.google.apphosting.api.ApiProxy").getMethod("getCurrentEnvironment", new Class[0]).invoke(null, new Object[0]) != null;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        catch (InvocationTargetException e) {
            return false;
        }
        catch (IllegalAccessException e) {
            return false;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    @J2ktIncompatible
    @GwtIncompatible
    static Thread newThread(String name, Runnable runnable) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(runnable);
        Thread result = Objects.requireNonNull(MoreExecutors.platformThreadFactory().newThread(runnable));
        try {
            result.setName(name);
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        return result;
    }

    @J2ktIncompatible
    @GwtIncompatible
    static Executor renamingDecorator(Executor executor, Supplier<String> nameSupplier) {
        Preconditions.checkNotNull(executor);
        Preconditions.checkNotNull(nameSupplier);
        return command -> executor.execute(Callables.threadRenaming(command, nameSupplier));
    }

    @J2ktIncompatible
    @GwtIncompatible
    static ExecutorService renamingDecorator(ExecutorService service, final Supplier<String> nameSupplier) {
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(nameSupplier);
        return new WrappingExecutorService(service){

            @Override
            protected <T> Callable<T> wrapTask(Callable<T> callable) {
                return Callables.threadRenaming(callable, (Supplier<String>)nameSupplier);
            }

            @Override
            protected Runnable wrapTask(Runnable command) {
                return Callables.threadRenaming(command, (Supplier<String>)nameSupplier);
            }
        };
    }

    @J2ktIncompatible
    @GwtIncompatible
    static ScheduledExecutorService renamingDecorator(ScheduledExecutorService service, final Supplier<String> nameSupplier) {
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(nameSupplier);
        return new WrappingScheduledExecutorService(service){

            @Override
            protected <T> Callable<T> wrapTask(Callable<T> callable) {
                return Callables.threadRenaming(callable, (Supplier<String>)nameSupplier);
            }

            @Override
            protected Runnable wrapTask(Runnable command) {
                return Callables.threadRenaming(command, (Supplier<String>)nameSupplier);
            }
        };
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static boolean shutdownAndAwaitTermination(ExecutorService service, Duration timeout) {
        return MoreExecutors.shutdownAndAwaitTermination(service, Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static boolean shutdownAndAwaitTermination(ExecutorService service, long timeout, TimeUnit unit) {
        long halfTimeoutNanos = unit.toNanos(timeout) / 2L;
        service.shutdown();
        try {
            if (!service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS)) {
                service.shutdownNow();
                service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS);
            }
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            service.shutdownNow();
        }
        return service.isTerminated();
    }

    static Executor rejectionPropagatingExecutor(Executor delegate, AbstractFuture<?> future) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkNotNull(future);
        if (delegate == MoreExecutors.directExecutor()) {
            return delegate;
        }
        return command -> {
            try {
                delegate.execute(command);
            }
            catch (RejectedExecutionException e) {
                future.setException(e);
            }
        };
    }

    private static /* synthetic */ void lambda$submitAndAddQueueListener$0(BlockingQueue queue, ListenableFuture future) {
        queue.add(future);
    }

    @J2ktIncompatible
    @GwtIncompatible
    @VisibleForTesting
    static class Application {
        Application() {
        }

        final ExecutorService getExitingExecutorService(ThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
            MoreExecutors.useDaemonThreadFactory(executor);
            ExecutorService service = Executors.unconfigurableExecutorService(executor);
            this.addDelayedShutdownHook(executor, terminationTimeout, timeUnit);
            return service;
        }

        final ExecutorService getExitingExecutorService(ThreadPoolExecutor executor) {
            return this.getExitingExecutorService(executor, 120L, TimeUnit.SECONDS);
        }

        final ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor, long terminationTimeout, TimeUnit timeUnit) {
            MoreExecutors.useDaemonThreadFactory(executor);
            ScheduledExecutorService service = Executors.unconfigurableScheduledExecutorService(executor);
            this.addDelayedShutdownHook(executor, terminationTimeout, timeUnit);
            return service;
        }

        final ScheduledExecutorService getExitingScheduledExecutorService(ScheduledThreadPoolExecutor executor) {
            return this.getExitingScheduledExecutorService(executor, 120L, TimeUnit.SECONDS);
        }

        final void addDelayedShutdownHook(ExecutorService service, long terminationTimeout, TimeUnit timeUnit) {
            Preconditions.checkNotNull(service);
            Preconditions.checkNotNull(timeUnit);
            this.addShutdownHook(MoreExecutors.newThread("DelayedShutdownHook-for-" + service, () -> {
                service.shutdown();
                try {
                    service.awaitTermination(terminationTimeout, timeUnit);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }));
        }

        @VisibleForTesting
        void addShutdownHook(Thread hook) {
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    @GwtIncompatible
    private static final class ScheduledListeningDecorator
    extends ListeningDecorator
    implements ListeningScheduledExecutorService {
        final ScheduledExecutorService delegate;

        ScheduledListeningDecorator(ScheduledExecutorService delegate) {
            super(delegate);
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            TrustedListenableFutureTask<@Nullable Object> task = TrustedListenableFutureTask.create(command, null);
            ScheduledFuture<?> scheduled = this.delegate.schedule(task, delay, unit);
            return new ListenableScheduledTask<Object>(task, scheduled);
        }

        @Override
        public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            TrustedListenableFutureTask<V> task = TrustedListenableFutureTask.create(callable);
            ScheduledFuture<?> scheduled = this.delegate.schedule(task, delay, unit);
            return new ListenableScheduledTask<V>(task, scheduled);
        }

        @Override
        public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            NeverSuccessfulListenableFutureTask task = new NeverSuccessfulListenableFutureTask(command);
            ScheduledFuture<?> scheduled = this.delegate.scheduleAtFixedRate(task, initialDelay, period, unit);
            return new ListenableScheduledTask<Void>(task, scheduled);
        }

        @Override
        public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            NeverSuccessfulListenableFutureTask task = new NeverSuccessfulListenableFutureTask(command);
            ScheduledFuture<?> scheduled = this.delegate.scheduleWithFixedDelay(task, initialDelay, delay, unit);
            return new ListenableScheduledTask<Void>(task, scheduled);
        }

        private static final class ListenableScheduledTask<V>
        extends ForwardingListenableFuture.SimpleForwardingListenableFuture<V>
        implements ListenableScheduledFuture<V> {
            private final ScheduledFuture<?> scheduledDelegate;

            ListenableScheduledTask(ListenableFuture<V> listenableDelegate, ScheduledFuture<?> scheduledDelegate) {
                super(listenableDelegate);
                this.scheduledDelegate = scheduledDelegate;
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled) {
                    this.scheduledDelegate.cancel(mayInterruptIfRunning);
                }
                return cancelled;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return this.scheduledDelegate.getDelay(unit);
            }

            @Override
            public int compareTo(Delayed other) {
                return this.scheduledDelegate.compareTo(other);
            }
        }

        @GwtIncompatible
        private static final class NeverSuccessfulListenableFutureTask
        extends AbstractFuture.TrustedFuture<Void>
        implements Runnable {
            private final Runnable delegate;

            NeverSuccessfulListenableFutureTask(Runnable delegate) {
                this.delegate = Preconditions.checkNotNull(delegate);
            }

            @Override
            public void run() {
                try {
                    this.delegate.run();
                }
                catch (Throwable t) {
                    this.setException(t);
                    throw t;
                }
            }

            @Override
            protected String pendingToString() {
                return "task=[" + this.delegate + "]";
            }
        }
    }

    @GwtIncompatible
    private static class ListeningDecorator
    extends AbstractListeningExecutorService {
        private final ExecutorService delegate;

        ListeningDecorator(ExecutorService delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return this.delegate.awaitTermination(timeout, unit);
        }

        @Override
        public final boolean isShutdown() {
            return this.delegate.isShutdown();
        }

        @Override
        public final boolean isTerminated() {
            return this.delegate.isTerminated();
        }

        @Override
        public final void shutdown() {
            this.delegate.shutdown();
        }

        @Override
        public final List<Runnable> shutdownNow() {
            return this.delegate.shutdownNow();
        }

        @Override
        public final void execute(Runnable command) {
            this.delegate.execute(command);
        }

        public final String toString() {
            return super.toString() + "[" + this.delegate + "]";
        }
    }
}

