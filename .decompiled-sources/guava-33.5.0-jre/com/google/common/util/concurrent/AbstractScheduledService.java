/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.Internal;
import com.google.common.util.concurrent.LazyLogger;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Platform;
import com.google.common.util.concurrent.Service;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
@J2ktIncompatible
public abstract class AbstractScheduledService
implements Service {
    private static final LazyLogger logger = new LazyLogger(AbstractScheduledService.class);
    private final AbstractService delegate = new ServiceDelegate();

    protected AbstractScheduledService() {
    }

    protected abstract void runOneIteration() throws Exception;

    protected void startUp() throws Exception {
    }

    protected void shutDown() throws Exception {
    }

    protected abstract Scheduler scheduler();

    protected ScheduledExecutorService executor() {
        final class ThreadFactoryImpl
        implements ThreadFactory {
            ThreadFactoryImpl() {
            }

            @Override
            public Thread newThread(Runnable runnable) {
                return MoreExecutors.newThread(AbstractScheduledService.this.serviceName(), runnable);
            }
        }
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl());
        this.addListener(new Service.Listener(this){
            final /* synthetic */ AbstractScheduledService this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public void terminated(Service.State from) {
                executor.shutdown();
            }

            @Override
            public void failed(Service.State from, Throwable failure) {
                executor.shutdown();
            }
        }, MoreExecutors.directExecutor());
        return executor;
    }

    protected String serviceName() {
        return this.getClass().getSimpleName();
    }

    public String toString() {
        return this.serviceName() + " [" + (Object)((Object)this.state()) + "]";
    }

    @Override
    public final boolean isRunning() {
        return this.delegate.isRunning();
    }

    @Override
    public final Service.State state() {
        return this.delegate.state();
    }

    @Override
    public final void addListener(Service.Listener listener, Executor executor) {
        this.delegate.addListener(listener, executor);
    }

    @Override
    public final Throwable failureCause() {
        return this.delegate.failureCause();
    }

    @Override
    @CanIgnoreReturnValue
    public final Service startAsync() {
        this.delegate.startAsync();
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public final Service stopAsync() {
        this.delegate.stopAsync();
        return this;
    }

    @Override
    public final void awaitRunning() {
        this.delegate.awaitRunning();
    }

    @Override
    public final void awaitRunning(Duration timeout) throws TimeoutException {
        Service.super.awaitRunning(timeout);
    }

    @Override
    public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
        this.delegate.awaitRunning(timeout, unit);
    }

    @Override
    public final void awaitTerminated() {
        this.delegate.awaitTerminated();
    }

    @Override
    public final void awaitTerminated(Duration timeout) throws TimeoutException {
        Service.super.awaitTerminated(timeout);
    }

    @Override
    public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
        this.delegate.awaitTerminated(timeout, unit);
    }

    private final class ServiceDelegate
    extends AbstractService {
        private volatile @Nullable Cancellable runningTask;
        private volatile @Nullable ScheduledExecutorService executorService;
        private final ReentrantLock lock = new ReentrantLock();
        private final Runnable task = new Task();

        private ServiceDelegate() {
        }

        @Override
        protected final void doStart() {
            this.executorService = MoreExecutors.renamingDecorator(AbstractScheduledService.this.executor(), () -> AbstractScheduledService.this.serviceName() + " " + (Object)((Object)this.state()));
            this.executorService.execute(() -> {
                this.lock.lock();
                try {
                    AbstractScheduledService.this.startUp();
                    Objects.requireNonNull(this.executorService);
                    this.runningTask = AbstractScheduledService.this.scheduler().schedule(AbstractScheduledService.this.delegate, this.executorService, this.task);
                    this.notifyStarted();
                }
                catch (Throwable t) {
                    Platform.restoreInterruptIfIsInterruptedException(t);
                    this.notifyFailed(t);
                    if (this.runningTask != null) {
                        this.runningTask.cancel(false);
                    }
                }
                finally {
                    this.lock.unlock();
                }
            });
        }

        @Override
        protected final void doStop() {
            Objects.requireNonNull(this.runningTask);
            Objects.requireNonNull(this.executorService);
            this.runningTask.cancel(false);
            this.executorService.execute(() -> {
                try {
                    this.lock.lock();
                    try {
                        if (this.state() != Service.State.STOPPING) {
                            return;
                        }
                        AbstractScheduledService.this.shutDown();
                    }
                    finally {
                        this.lock.unlock();
                    }
                    this.notifyStopped();
                }
                catch (Throwable t) {
                    Platform.restoreInterruptIfIsInterruptedException(t);
                    this.notifyFailed(t);
                }
            });
        }

        @Override
        public String toString() {
            return AbstractScheduledService.this.toString();
        }

        final class Task
        implements Runnable {
            Task() {
            }

            @Override
            public void run() {
                ServiceDelegate.this.lock.lock();
                try {
                    if (Objects.requireNonNull(ServiceDelegate.this.runningTask).isCancelled()) {
                        return;
                    }
                    AbstractScheduledService.this.runOneIteration();
                }
                catch (Throwable t) {
                    Platform.restoreInterruptIfIsInterruptedException(t);
                    try {
                        AbstractScheduledService.this.shutDown();
                    }
                    catch (Exception ignored) {
                        Platform.restoreInterruptIfIsInterruptedException(ignored);
                        logger.get().log(Level.WARNING, "Error while attempting to shut down the service after failure.", ignored);
                    }
                    ServiceDelegate.this.notifyFailed(t);
                    Objects.requireNonNull(ServiceDelegate.this.runningTask).cancel(false);
                }
                finally {
                    ServiceDelegate.this.lock.unlock();
                }
            }
        }
    }

    public static abstract class CustomScheduler
    extends Scheduler {
        @Override
        final Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable) {
            return new ReschedulableCallable(service, executor, runnable).reschedule();
        }

        protected abstract Schedule getNextSchedule() throws Exception;

        private final class ReschedulableCallable
        implements Callable<Void> {
            private final Runnable wrappedRunnable;
            private final ScheduledExecutorService executor;
            private final AbstractService service;
            private final ReentrantLock lock = new ReentrantLock();
            @GuardedBy(value="lock")
            private @Nullable SupplantableFuture cancellationDelegate;

            ReschedulableCallable(AbstractService service, ScheduledExecutorService executor, Runnable runnable) {
                this.wrappedRunnable = runnable;
                this.executor = executor;
                this.service = service;
            }

            @Override
            public @Nullable Void call() throws Exception {
                this.wrappedRunnable.run();
                this.reschedule();
                return null;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @CanIgnoreReturnValue
            Cancellable reschedule() {
                Cancellable toReturn;
                Schedule schedule;
                try {
                    schedule = CustomScheduler.this.getNextSchedule();
                }
                catch (Throwable t) {
                    Platform.restoreInterruptIfIsInterruptedException(t);
                    this.service.notifyFailed(t);
                    return new FutureAsCancellable(Futures.immediateCancelledFuture());
                }
                Throwable scheduleFailure = null;
                this.lock.lock();
                try {
                    toReturn = this.initializeOrUpdateCancellationDelegate(schedule);
                }
                catch (Throwable e) {
                    scheduleFailure = e;
                    toReturn = new FutureAsCancellable(Futures.immediateCancelledFuture());
                }
                finally {
                    this.lock.unlock();
                }
                if (scheduleFailure != null) {
                    this.service.notifyFailed(scheduleFailure);
                }
                return toReturn;
            }

            @GuardedBy(value="lock")
            private Cancellable initializeOrUpdateCancellationDelegate(Schedule schedule) {
                if (this.cancellationDelegate == null) {
                    this.cancellationDelegate = new SupplantableFuture(this.lock, this.submitToExecutor(schedule));
                    return this.cancellationDelegate;
                }
                if (!this.cancellationDelegate.currentFuture.isCancelled()) {
                    this.cancellationDelegate.currentFuture = this.submitToExecutor(schedule);
                }
                return this.cancellationDelegate;
            }

            private ScheduledFuture<@Nullable Void> submitToExecutor(Schedule schedule) {
                return this.executor.schedule(this, schedule.delay, schedule.unit);
            }
        }

        protected static final class Schedule {
            private final long delay;
            private final TimeUnit unit;

            public Schedule(long delay, TimeUnit unit) {
                this.delay = delay;
                this.unit = Preconditions.checkNotNull(unit);
            }

            public Schedule(Duration delay) {
                this(Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
            }
        }

        private static final class SupplantableFuture
        implements Cancellable {
            private final ReentrantLock lock;
            @GuardedBy(value="lock")
            private Future<@Nullable Void> currentFuture;

            SupplantableFuture(ReentrantLock lock, Future<@Nullable Void> currentFuture) {
                this.lock = lock;
                this.currentFuture = currentFuture;
            }

            @Override
            public void cancel(boolean mayInterruptIfRunning) {
                this.lock.lock();
                try {
                    this.currentFuture.cancel(mayInterruptIfRunning);
                }
                finally {
                    this.lock.unlock();
                }
            }

            @Override
            public boolean isCancelled() {
                this.lock.lock();
                try {
                    boolean bl = this.currentFuture.isCancelled();
                    return bl;
                }
                finally {
                    this.lock.unlock();
                }
            }
        }
    }

    private static final class FutureAsCancellable
    implements Cancellable {
        private final Future<?> delegate;

        FutureAsCancellable(Future<?> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void cancel(boolean mayInterruptIfRunning) {
            this.delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return this.delegate.isCancelled();
        }
    }

    static interface Cancellable {
        public void cancel(boolean var1);

        public boolean isCancelled();
    }

    public static abstract class Scheduler {
        public static Scheduler newFixedDelaySchedule(Duration initialDelay, Duration delay) {
            return Scheduler.newFixedDelaySchedule(Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(delay), TimeUnit.NANOSECONDS);
        }

        public static Scheduler newFixedDelaySchedule(final long initialDelay, final long delay, final TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            Preconditions.checkArgument(delay > 0L, "delay must be > 0, found %s", delay);
            return new Scheduler(){

                @Override
                public Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable task) {
                    return new FutureAsCancellable(executor.scheduleWithFixedDelay(task, initialDelay, delay, unit));
                }
            };
        }

        public static Scheduler newFixedRateSchedule(Duration initialDelay, Duration period) {
            return Scheduler.newFixedRateSchedule(Internal.toNanosSaturated(initialDelay), Internal.toNanosSaturated(period), TimeUnit.NANOSECONDS);
        }

        public static Scheduler newFixedRateSchedule(final long initialDelay, final long period, final TimeUnit unit) {
            Preconditions.checkNotNull(unit);
            Preconditions.checkArgument(period > 0L, "period must be > 0, found %s", period);
            return new Scheduler(){

                @Override
                public Cancellable schedule(AbstractService service, ScheduledExecutorService executor, Runnable task) {
                    return new FutureAsCancellable(executor.scheduleAtFixedRate(task, initialDelay, period, unit));
                }
            };
        }

        abstract Cancellable schedule(AbstractService var1, ScheduledExecutorService var2, Runnable var3);

        private Scheduler() {
        }
    }
}

