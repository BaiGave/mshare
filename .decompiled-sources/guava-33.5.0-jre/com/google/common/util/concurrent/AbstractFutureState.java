/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.IgnoreJRERequirement;
import com.google.common.util.concurrent.LazyLogger;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.OverflowAvoidingLockSupport;
import com.google.common.util.concurrent.ParametricNullness;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.j2objc.annotations.ReflectionSupport;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

@GwtCompatible
@ReflectionSupport(value=ReflectionSupport.Level.FULL)
abstract class AbstractFutureState<V>
extends InternalFutureFailureAccess
implements ListenableFuture<V> {
    static final Object NULL;
    static final LazyLogger log;
    static final boolean GENERATE_CANCELLATION_CAUSES;
    private static final AtomicHelper ATOMIC_HELPER;
    volatile @Nullable Object valueField;
    volatile @Nullable AbstractFuture.Listener listenersField;
    volatile @Nullable Waiter waitersField;
    private static final long SPIN_THRESHOLD_NANOS = 1000L;

    final boolean casListeners(@Nullable AbstractFuture.Listener expect, AbstractFuture.Listener update) {
        return ATOMIC_HELPER.casListeners(this, expect, update);
    }

    final @Nullable AbstractFuture.Listener gasListeners(AbstractFuture.Listener update) {
        return ATOMIC_HELPER.gasListeners(this, update);
    }

    static boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
        return ATOMIC_HELPER.casValue(future, expect, update);
    }

    final @Nullable Object value() {
        return this.valueField;
    }

    final @Nullable AbstractFuture.Listener listeners() {
        return this.listenersField;
    }

    final void releaseWaiters() {
        Waiter head;
        Waiter currentWaiter = head = this.gasWaiters(Waiter.TOMBSTONE);
        while (currentWaiter != null) {
            currentWaiter.unpark();
            currentWaiter = currentWaiter.next;
        }
    }

    @ParametricNullness
    final V blockingGet(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        long endNanos;
        Object localValue;
        long remainingNanos;
        block15: {
            long timeoutNanos;
            remainingNanos = timeoutNanos = unit.toNanos(timeout);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            localValue = this.valueField;
            if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
                return AbstractFuture.getDoneValue(localValue);
            }
            long l = endNanos = remainingNanos > 0L ? System.nanoTime() + remainingNanos : 0L;
            if (remainingNanos >= 1000L) {
                Waiter oldHead = this.waitersField;
                if (oldHead != Waiter.TOMBSTONE) {
                    Waiter node = new Waiter();
                    do {
                        node.setNext(oldHead);
                        if (!this.casWaiters(oldHead, node)) continue;
                        do {
                            OverflowAvoidingLockSupport.parkNanos(this, remainingNanos);
                            if (Thread.interrupted()) {
                                this.removeWaiter(node);
                                throw new InterruptedException();
                            }
                            localValue = this.valueField;
                            if (!(localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue))) continue;
                            return AbstractFuture.getDoneValue(localValue);
                        } while ((remainingNanos = endNanos - System.nanoTime()) >= 1000L);
                        this.removeWaiter(node);
                        break block15;
                    } while ((oldHead = this.waitersField) != Waiter.TOMBSTONE);
                }
                return AbstractFuture.getDoneValue(Objects.requireNonNull(this.valueField));
            }
        }
        while (remainingNanos > 0L) {
            localValue = this.valueField;
            if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
                return AbstractFuture.getDoneValue(localValue);
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            remainingNanos = endNanos - System.nanoTime();
        }
        String futureToString = this.toString();
        String unitString = unit.toString().toLowerCase(Locale.ROOT);
        String message = "Waited " + timeout + " " + unit.toString().toLowerCase(Locale.ROOT);
        if (remainingNanos + 1000L < 0L) {
            boolean shouldShowExtraNanos;
            message = message + " (plus ";
            long overWaitNanos = -remainingNanos;
            long overWaitUnits = unit.convert(overWaitNanos, TimeUnit.NANOSECONDS);
            long overWaitLeftoverNanos = overWaitNanos - unit.toNanos(overWaitUnits);
            boolean bl = shouldShowExtraNanos = overWaitUnits == 0L || overWaitLeftoverNanos > 1000L;
            if (overWaitUnits > 0L) {
                message = message + overWaitUnits + " " + unitString;
                if (shouldShowExtraNanos) {
                    message = message + ",";
                }
                message = message + " ";
            }
            if (shouldShowExtraNanos) {
                message = message + overWaitLeftoverNanos + " nanoseconds ";
            }
            message = message + "delay)";
        }
        if (this.isDone()) {
            throw new TimeoutException(message + " but future completed as timeout expired");
        }
        throw new TimeoutException(message + " for " + futureToString);
    }

    @ParametricNullness
    final V blockingGet() throws InterruptedException, ExecutionException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        Object localValue = this.valueField;
        if (localValue != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)) {
            return AbstractFuture.getDoneValue(localValue);
        }
        Waiter oldHead = this.waitersField;
        if (oldHead != Waiter.TOMBSTONE) {
            Waiter node = new Waiter();
            do {
                node.setNext(oldHead);
                if (!this.casWaiters(oldHead, node)) continue;
                do {
                    LockSupport.park(this);
                    if (!Thread.interrupted()) continue;
                    this.removeWaiter(node);
                    throw new InterruptedException();
                } while (!((localValue = this.valueField) != null & AbstractFuture.notInstanceOfDelegatingToFuture(localValue)));
                return AbstractFuture.getDoneValue(localValue);
            } while ((oldHead = this.waitersField) != Waiter.TOMBSTONE);
        }
        return AbstractFuture.getDoneValue(Objects.requireNonNull(this.valueField));
    }

    AbstractFutureState() {
    }

    private static void putThread(Waiter waiter, Thread newValue) {
        ATOMIC_HELPER.putThread(waiter, newValue);
    }

    private static void putNext(Waiter waiter, @Nullable Waiter newValue) {
        ATOMIC_HELPER.putNext(waiter, newValue);
    }

    private boolean casWaiters(@Nullable Waiter expect, @Nullable Waiter update) {
        return ATOMIC_HELPER.casWaiters(this, expect, update);
    }

    private final @Nullable Waiter gasWaiters(Waiter update) {
        return ATOMIC_HELPER.gasWaiters(this, update);
    }

    /*
     * Unable to fully structure code
     */
    private void removeWaiter(Waiter node) {
        node.thread = null;
        block0: while (true) {
            pred = null;
            curr = this.waitersField;
            if (curr == Waiter.TOMBSTONE) {
                return;
            }
            while (curr != null) {
                succ = curr.next;
                if (curr.thread != null) {
                    pred = curr;
                } else if (pred != null) {
                    pred.next = succ;
                    if (pred.thread == null) {
                        continue block0;
                    }
                } else {
                    if (this.casWaiters(curr, succ)) ** break;
                    continue block0;
                }
                curr = succ;
            }
            break;
        }
    }

    @VisibleForTesting
    static String atomicHelperTypeForTest() {
        return ATOMIC_HELPER.atomicHelperTypeForTest();
    }

    static {
        boolean generateCancellationCauses;
        NULL = new Object();
        log = new LazyLogger(AbstractFuture.class);
        try {
            generateCancellationCauses = Boolean.parseBoolean(System.getProperty("guava.concurrent.generate_cancellation_cause", "false"));
        }
        catch (SecurityException e) {
            generateCancellationCauses = false;
        }
        GENERATE_CANCELLATION_CAUSES = generateCancellationCauses;
        Throwable thrownUnsafeFailure = null;
        Throwable thrownAtomicReferenceFieldUpdaterFailure = null;
        AtomicHelper helper = VarHandleAtomicHelperMaker.INSTANCE.tryMakeVarHandleAtomicHelper();
        if (helper == null) {
            try {
                helper = new UnsafeAtomicHelper();
            }
            catch (Error | Exception unsafeFailure) {
                thrownUnsafeFailure = unsafeFailure;
                try {
                    helper = new AtomicReferenceFieldUpdaterAtomicHelper();
                }
                catch (Error | Exception atomicReferenceFieldUpdaterFailure) {
                    thrownAtomicReferenceFieldUpdaterFailure = atomicReferenceFieldUpdaterFailure;
                    helper = new SynchronizedHelper();
                }
            }
        }
        ATOMIC_HELPER = helper;
        Class<LockSupport> ensureLoaded = LockSupport.class;
        if (thrownAtomicReferenceFieldUpdaterFailure != null) {
            log.get().log(Level.SEVERE, "UnsafeAtomicHelper is broken!", thrownUnsafeFailure);
            log.get().log(Level.SEVERE, "AtomicReferenceFieldUpdaterAtomicHelper is broken!", thrownAtomicReferenceFieldUpdaterFailure);
        }
    }

    static final class Waiter {
        static final Waiter TOMBSTONE = new Waiter(false);
        volatile @Nullable Thread thread;
        volatile @Nullable Waiter next;

        Waiter(boolean unused) {
        }

        Waiter() {
            AbstractFutureState.putThread(this, Thread.currentThread());
        }

        void setNext(@Nullable Waiter next) {
            AbstractFutureState.putNext(this, next);
        }

        void unpark() {
            Thread w = this.thread;
            if (w != null) {
                this.thread = null;
                LockSupport.unpark(w);
            }
        }
    }

    private static abstract class AtomicHelper {
        private AtomicHelper() {
        }

        abstract void putThread(Waiter var1, Thread var2);

        abstract void putNext(Waiter var1, @Nullable Waiter var2);

        abstract boolean casWaiters(AbstractFutureState<?> var1, @Nullable Waiter var2, @Nullable Waiter var3);

        abstract boolean casListeners(AbstractFutureState<?> var1, @Nullable AbstractFuture.Listener var2, AbstractFuture.Listener var3);

        abstract @Nullable Waiter gasWaiters(AbstractFutureState<?> var1, Waiter var2);

        abstract @Nullable AbstractFuture.Listener gasListeners(AbstractFutureState<?> var1, AbstractFuture.Listener var2);

        abstract boolean casValue(AbstractFutureState<?> var1, @Nullable Object var2, Object var3);

        abstract String atomicHelperTypeForTest();
    }

    private static enum VarHandleAtomicHelperMaker {
        INSTANCE{

            @Override
            @Nullable AtomicHelper tryMakeVarHandleAtomicHelper() {
                try {
                    Class.forName("java.lang.invoke.VarHandle");
                }
                catch (ClassNotFoundException beforeJava9) {
                    return null;
                }
                return new VarHandleAtomicHelper();
            }
        };


        @Nullable AtomicHelper tryMakeVarHandleAtomicHelper() {
            return null;
        }
    }

    private static final class UnsafeAtomicHelper
    extends AtomicHelper {
        static final Unsafe UNSAFE;
        static final long LISTENERS_OFFSET;
        static final long WAITERS_OFFSET;
        static final long VALUE_OFFSET;
        static final long WAITER_THREAD_OFFSET;
        static final long WAITER_NEXT_OFFSET;

        private UnsafeAtomicHelper() {
        }

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            UNSAFE.putObject(waiter, WAITER_THREAD_OFFSET, newValue);
        }

        @Override
        void putNext(Waiter waiter, @Nullable Waiter newValue) {
            UNSAFE.putObject(waiter, WAITER_NEXT_OFFSET, newValue);
        }

        @Override
        boolean casWaiters(AbstractFutureState<?> future, @Nullable Waiter expect, @Nullable Waiter update) {
            return UNSAFE.compareAndSwapObject(future, WAITERS_OFFSET, expect, update);
        }

        @Override
        boolean casListeners(AbstractFutureState<?> future, @Nullable AbstractFuture.Listener expect, AbstractFuture.Listener update) {
            return UNSAFE.compareAndSwapObject(future, LISTENERS_OFFSET, expect, update);
        }

        @Override
        @Nullable AbstractFuture.Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
            return (AbstractFuture.Listener)UNSAFE.getAndSetObject(future, LISTENERS_OFFSET, update);
        }

        @Override
        @Nullable Waiter gasWaiters(AbstractFutureState<?> future, Waiter update) {
            return (Waiter)UNSAFE.getAndSetObject(future, WAITERS_OFFSET, update);
        }

        @Override
        boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
            return UNSAFE.compareAndSwapObject(future, VALUE_OFFSET, expect, update);
        }

        @Override
        String atomicHelperTypeForTest() {
            return "UnsafeAtomicHelper";
        }

        static {
            Unsafe unsafe = null;
            try {
                unsafe = Unsafe.getUnsafe();
            }
            catch (SecurityException tryReflectionInstead) {
                try {
                    unsafe = AccessController.doPrivileged(() -> {
                        Class<Unsafe> k = Unsafe.class;
                        for (Field f : k.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (!k.isInstance(x)) continue;
                            return (Unsafe)k.cast(x);
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    });
                }
                catch (PrivilegedActionException e) {
                    throw new RuntimeException("Could not initialize intrinsics", e.getCause());
                }
            }
            try {
                Class<AbstractFutureState> abstractFutureState = AbstractFutureState.class;
                WAITERS_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("waitersField"));
                LISTENERS_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("listenersField"));
                VALUE_OFFSET = unsafe.objectFieldOffset(abstractFutureState.getDeclaredField("valueField"));
                WAITER_THREAD_OFFSET = unsafe.objectFieldOffset(Waiter.class.getDeclaredField("thread"));
                WAITER_NEXT_OFFSET = unsafe.objectFieldOffset(Waiter.class.getDeclaredField("next"));
                UNSAFE = unsafe;
            }
            catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class AtomicReferenceFieldUpdaterAtomicHelper
    extends AtomicHelper {
        private static final AtomicReferenceFieldUpdater<Waiter, @Nullable Thread> waiterThreadUpdater = AtomicReferenceFieldUpdater.newUpdater(Waiter.class, Thread.class, "thread");
        private static final AtomicReferenceFieldUpdater<Waiter, @Nullable Waiter> waiterNextUpdater = AtomicReferenceFieldUpdater.newUpdater(Waiter.class, Waiter.class, "next");
        private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, @Nullable Waiter> waitersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFutureState.class, Waiter.class, "waitersField");
        private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, @Nullable AbstractFuture.Listener> listenersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFutureState.class, AbstractFuture.Listener.class, "listenersField");
        private static final AtomicReferenceFieldUpdater<? super AbstractFutureState<?>, @Nullable Object> valueUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFutureState.class, Object.class, "valueField");

        private AtomicReferenceFieldUpdaterAtomicHelper() {
        }

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            waiterThreadUpdater.lazySet(waiter, newValue);
        }

        @Override
        void putNext(Waiter waiter, @Nullable Waiter newValue) {
            waiterNextUpdater.lazySet(waiter, newValue);
        }

        @Override
        boolean casWaiters(AbstractFutureState<?> future, @Nullable Waiter expect, @Nullable Waiter update) {
            return waitersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        boolean casListeners(AbstractFutureState<?> future, @Nullable AbstractFuture.Listener expect, AbstractFuture.Listener update) {
            return listenersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        @Nullable AbstractFuture.Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
            return listenersUpdater.getAndSet(future, update);
        }

        @Override
        @Nullable Waiter gasWaiters(AbstractFutureState<?> future, Waiter update) {
            return waitersUpdater.getAndSet(future, update);
        }

        @Override
        boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
            return valueUpdater.compareAndSet(future, expect, update);
        }

        @Override
        String atomicHelperTypeForTest() {
            return "AtomicReferenceFieldUpdaterAtomicHelper";
        }
    }

    private static final class SynchronizedHelper
    extends AtomicHelper {
        private SynchronizedHelper() {
        }

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            waiter.thread = newValue;
        }

        @Override
        void putNext(Waiter waiter, @Nullable Waiter newValue) {
            waiter.next = newValue;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean casWaiters(AbstractFutureState<?> future, @Nullable Waiter expect, @Nullable Waiter update) {
            AbstractFutureState<?> abstractFutureState = future;
            synchronized (abstractFutureState) {
                if (future.waitersField == expect) {
                    future.waitersField = update;
                    return true;
                }
                return false;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean casListeners(AbstractFutureState<?> future, @Nullable AbstractFuture.Listener expect, AbstractFuture.Listener update) {
            AbstractFutureState<?> abstractFutureState = future;
            synchronized (abstractFutureState) {
                if (future.listenersField == expect) {
                    future.listenersField = update;
                    return true;
                }
                return false;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable AbstractFuture.Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
            AbstractFutureState<?> abstractFutureState = future;
            synchronized (abstractFutureState) {
                AbstractFuture.Listener old = future.listenersField;
                if (old != update) {
                    future.listenersField = update;
                }
                return old;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable Waiter gasWaiters(AbstractFutureState<?> future, Waiter update) {
            AbstractFutureState<?> abstractFutureState = future;
            synchronized (abstractFutureState) {
                Waiter old = future.waitersField;
                if (old != update) {
                    future.waitersField = update;
                }
                return old;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
            AbstractFutureState<?> abstractFutureState = future;
            synchronized (abstractFutureState) {
                if (future.valueField == expect) {
                    future.valueField = update;
                    return true;
                }
                return false;
            }
        }

        @Override
        String atomicHelperTypeForTest() {
            return "SynchronizedHelper";
        }
    }

    @IgnoreJRERequirement
    private static final class VarHandleAtomicHelper
    extends AtomicHelper {
        static final VarHandle waiterThreadUpdater;
        static final VarHandle waiterNextUpdater;
        static final VarHandle waitersUpdater;
        static final VarHandle listenersUpdater;
        static final VarHandle valueUpdater;

        private VarHandleAtomicHelper() {
        }

        @Override
        void putThread(Waiter waiter, Thread newValue) {
            waiterThreadUpdater.setRelease(waiter, newValue);
        }

        @Override
        void putNext(Waiter waiter, @Nullable Waiter newValue) {
            waiterNextUpdater.setRelease(waiter, newValue);
        }

        @Override
        boolean casWaiters(AbstractFutureState<?> future, @Nullable Waiter expect, @Nullable Waiter update) {
            return waitersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        boolean casListeners(AbstractFutureState<?> future, @Nullable AbstractFuture.Listener expect, AbstractFuture.Listener update) {
            return listenersUpdater.compareAndSet(future, expect, update);
        }

        @Override
        @Nullable AbstractFuture.Listener gasListeners(AbstractFutureState<?> future, AbstractFuture.Listener update) {
            return listenersUpdater.getAndSet(future, update);
        }

        @Override
        @Nullable Waiter gasWaiters(AbstractFutureState<?> future, Waiter update) {
            return waitersUpdater.getAndSet(future, update);
        }

        @Override
        boolean casValue(AbstractFutureState<?> future, @Nullable Object expect, Object update) {
            return valueUpdater.compareAndSet(future, expect, update);
        }

        private static LinkageError newLinkageError(Throwable cause) {
            return new LinkageError(cause.toString(), cause);
        }

        @Override
        String atomicHelperTypeForTest() {
            return "VarHandleAtomicHelper";
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                waiterThreadUpdater = lookup.findVarHandle(Waiter.class, "thread", Thread.class);
                waiterNextUpdater = lookup.findVarHandle(Waiter.class, "next", Waiter.class);
                waitersUpdater = lookup.findVarHandle(AbstractFutureState.class, "waitersField", Waiter.class);
                listenersUpdater = lookup.findVarHandle(AbstractFutureState.class, "listenersField", AbstractFuture.Listener.class);
                valueUpdater = lookup.findVarHandle(AbstractFutureState.class, "valueField", Object.class);
            }
            catch (ReflectiveOperationException e) {
                throw VarHandleAtomicHelper.newLinkageError(e);
            }
        }
    }
}

