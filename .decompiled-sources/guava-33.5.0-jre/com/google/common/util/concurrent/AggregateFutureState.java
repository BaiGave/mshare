/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.LazyLogger;
import com.google.j2objc.annotations.ReflectionSupport;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@GwtCompatible
@ReflectionSupport(value=ReflectionSupport.Level.FULL)
abstract class AggregateFutureState<OutputT>
extends AbstractFuture.TrustedFuture<OutputT> {
    volatile @Nullable Set<Throwable> seenExceptionsField = null;
    volatile int remainingField;
    private static final AtomicHelper ATOMIC_HELPER;
    private static final LazyLogger log;

    AggregateFutureState(int remainingFutures) {
        this.remainingField = remainingFutures;
    }

    final Set<Throwable> getOrInitSeenExceptions() {
        Set<Throwable> seenExceptionsLocal = this.seenExceptionsField;
        if (seenExceptionsLocal == null) {
            seenExceptionsLocal = Sets.newConcurrentHashSet();
            this.addInitialException(seenExceptionsLocal);
            ATOMIC_HELPER.compareAndSetSeenExceptions(this, null, seenExceptionsLocal);
            seenExceptionsLocal = Objects.requireNonNull(this.seenExceptionsField);
        }
        return seenExceptionsLocal;
    }

    abstract void addInitialException(Set<Throwable> var1);

    final int decrementRemainingAndGet() {
        return ATOMIC_HELPER.decrementAndGetRemainingCount(this);
    }

    final void clearSeenExceptions() {
        this.seenExceptionsField = null;
    }

    @VisibleForTesting
    static String atomicHelperTypeForTest() {
        return ATOMIC_HELPER.atomicHelperTypeForTest();
    }

    static {
        AtomicHelper helper;
        log = new LazyLogger(AggregateFutureState.class);
        Throwable thrownReflectionFailure = null;
        try {
            helper = new SafeAtomicHelper();
        }
        catch (Throwable reflectionFailure) {
            thrownReflectionFailure = reflectionFailure;
            helper = new SynchronizedAtomicHelper();
        }
        ATOMIC_HELPER = helper;
        if (thrownReflectionFailure != null) {
            log.get().log(Level.SEVERE, "SafeAtomicHelper is broken!", thrownReflectionFailure);
        }
    }

    private static abstract class AtomicHelper {
        private AtomicHelper() {
        }

        abstract void compareAndSetSeenExceptions(AggregateFutureState<?> var1, @Nullable Set<Throwable> var2, Set<Throwable> var3);

        abstract int decrementAndGetRemainingCount(AggregateFutureState<?> var1);

        abstract String atomicHelperTypeForTest();
    }

    private static final class SafeAtomicHelper
    extends AtomicHelper {
        private static final AtomicReferenceFieldUpdater<? super AggregateFutureState<?>, ? super @Nullable Set<Throwable>> seenExceptionsUpdater = AtomicReferenceFieldUpdater.newUpdater(AggregateFutureState.class, Set.class, "seenExceptionsField");
        private static final AtomicIntegerFieldUpdater<? super AggregateFutureState<?>> remainingCountUpdater = AtomicIntegerFieldUpdater.newUpdater(AggregateFutureState.class, "remainingField");

        private SafeAtomicHelper() {
        }

        @Override
        void compareAndSetSeenExceptions(AggregateFutureState<?> state, @Nullable Set<Throwable> expect, Set<Throwable> update) {
            seenExceptionsUpdater.compareAndSet(state, expect, update);
        }

        @Override
        int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
            return remainingCountUpdater.decrementAndGet(state);
        }

        @Override
        String atomicHelperTypeForTest() {
            return "SafeAtomicHelper";
        }
    }

    private static final class SynchronizedAtomicHelper
    extends AtomicHelper {
        private SynchronizedAtomicHelper() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        void compareAndSetSeenExceptions(AggregateFutureState<?> state, @Nullable Set<Throwable> expect, Set<Throwable> update) {
            AggregateFutureState<?> aggregateFutureState = state;
            synchronized (aggregateFutureState) {
                if (state.seenExceptionsField == expect) {
                    state.seenExceptionsField = update;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
            AggregateFutureState<?> aggregateFutureState = state;
            synchronized (aggregateFutureState) {
                return --state.remainingField;
            }
        }

        @Override
        String atomicHelperTypeForTest() {
            return "SynchronizedAtomicHelper";
        }
    }
}

