/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.IgnoreJRERequirement;
import com.google.common.base.Internal;
import com.google.common.base.NullnessCasts;
import com.google.common.base.ParametricNullness;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Suppliers {
    private Suppliers() {
    }

    public static <F, T> Supplier<T> compose(Function<? super F, T> function, Supplier<F> supplier) {
        return new SupplierComposition<F, T>(function, supplier);
    }

    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        if (delegate instanceof NonSerializableMemoizingSupplier || delegate instanceof MemoizingSupplier) {
            return delegate;
        }
        return delegate instanceof Serializable ? new MemoizingSupplier<T>(delegate) : new NonSerializableMemoizingSupplier<T>(delegate);
    }

    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkArgument(duration > 0L, "duration (%s %s) must be > 0", duration, (Object)unit);
        return new ExpiringMemoizingSupplier<T>(delegate, unit.toNanos(duration));
    }

    @J2ktIncompatible
    @GwtIncompatible
    @IgnoreJRERequirement
    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, Duration duration) {
        Preconditions.checkNotNull(delegate);
        Preconditions.checkArgument(!duration.isNegative() && !duration.isZero(), "duration (%s) must be > 0", (Object)duration);
        return new ExpiringMemoizingSupplier<T>(delegate, Internal.toNanosSaturated(duration));
    }

    public static <T> Supplier<T> ofInstance(@ParametricNullness T instance) {
        return new SupplierOfInstance<T>(instance);
    }

    @J2ktIncompatible
    public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
        return new ThreadSafeSupplier<T>(delegate);
    }

    public static <T> Function<Supplier<T>, T> supplierFunction() {
        SupplierFunctionImpl sf = SupplierFunctionImpl.INSTANCE;
        return sf;
    }

    private static final class SupplierComposition<F, T>
    implements Supplier<T>,
    Serializable {
        final Function<? super F, T> function;
        final Supplier<F> supplier;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SupplierComposition(Function<? super F, T> function, Supplier<F> supplier) {
            this.function = Preconditions.checkNotNull(function);
            this.supplier = Preconditions.checkNotNull(supplier);
        }

        @Override
        @ParametricNullness
        public T get() {
            return this.function.apply(this.supplier.get());
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierComposition) {
                SupplierComposition that = (SupplierComposition)obj;
                return this.function.equals(that.function) && this.supplier.equals(that.supplier);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(this.function, this.supplier);
        }

        public String toString() {
            return "Suppliers.compose(" + this.function + ", " + this.supplier + ")";
        }
    }

    private static final class NonSerializableMemoizingSupplier<T>
    implements Supplier<T> {
        private final Object lock = new Object();
        private static final Supplier<@Nullable Void> SUCCESSFULLY_COMPUTED = () -> {
            throw new IllegalStateException();
        };
        private volatile Supplier<T> delegate;
        private @Nullable T value;

        NonSerializableMemoizingSupplier(Supplier<T> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @ParametricNullness
        public T get() {
            if (this.delegate != SUCCESSFULLY_COMPUTED) {
                Object object = this.lock;
                synchronized (object) {
                    if (this.delegate != SUCCESSFULLY_COMPUTED) {
                        T t = this.delegate.get();
                        this.value = t;
                        this.delegate = SUCCESSFULLY_COMPUTED;
                        return t;
                    }
                }
            }
            return NullnessCasts.uncheckedCastNullableTToT(this.value);
        }

        public String toString() {
            Supplier<T> delegate = this.delegate;
            return "Suppliers.memoize(" + (delegate == SUCCESSFULLY_COMPUTED ? "<supplier that returned " + this.value + ">" : delegate) + ")";
        }
    }

    @VisibleForTesting
    static final class MemoizingSupplier<T>
    implements Supplier<T>,
    Serializable {
        private transient Object lock = new Object();
        final Supplier<T> delegate;
        volatile transient boolean initialized;
        transient @Nullable T value;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @ParametricNullness
        public T get() {
            if (!this.initialized) {
                Object object = this.lock;
                synchronized (object) {
                    if (!this.initialized) {
                        T t = this.delegate.get();
                        this.value = t;
                        this.initialized = true;
                        return t;
                    }
                }
            }
            return NullnessCasts.uncheckedCastNullableTToT(this.value);
        }

        public String toString() {
            return "Suppliers.memoize(" + (this.initialized ? "<supplier that returned " + this.value + ">" : this.delegate) + ")";
        }

        @GwtIncompatible
        @J2ktIncompatible
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.lock = new Object();
        }
    }

    @VisibleForTesting
    static final class ExpiringMemoizingSupplier<T>
    implements Supplier<T>,
    Serializable {
        private transient Object lock = new Object();
        final Supplier<T> delegate;
        final long durationNanos;
        volatile transient @Nullable T value;
        volatile transient long expirationNanos;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        ExpiringMemoizingSupplier(Supplier<T> delegate, long durationNanos) {
            this.delegate = delegate;
            this.durationNanos = durationNanos;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @ParametricNullness
        public T get() {
            long nanos = this.expirationNanos;
            long now = System.nanoTime();
            if (nanos == 0L || now - nanos >= 0L) {
                Object object = this.lock;
                synchronized (object) {
                    if (nanos == this.expirationNanos) {
                        T t = this.delegate.get();
                        this.value = t;
                        nanos = now + this.durationNanos;
                        this.expirationNanos = nanos == 0L ? 1L : nanos;
                        return t;
                    }
                }
            }
            return NullnessCasts.uncheckedCastNullableTToT(this.value);
        }

        public String toString() {
            return "Suppliers.memoizeWithExpiration(" + this.delegate + ", " + this.durationNanos + ", NANOS)";
        }

        @GwtIncompatible
        @J2ktIncompatible
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.lock = new Object();
        }
    }

    private static final class SupplierOfInstance<T>
    implements Supplier<T>,
    Serializable {
        @ParametricNullness
        final T instance;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SupplierOfInstance(@ParametricNullness T instance) {
            this.instance = instance;
        }

        @Override
        @ParametricNullness
        public T get() {
            return this.instance;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierOfInstance) {
                SupplierOfInstance that = (SupplierOfInstance)obj;
                return Objects.equals(this.instance, that.instance);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(this.instance);
        }

        public String toString() {
            return "Suppliers.ofInstance(" + this.instance + ")";
        }
    }

    @J2ktIncompatible
    private static final class ThreadSafeSupplier<T>
    implements Supplier<T>,
    Serializable {
        final Supplier<T> delegate;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        ThreadSafeSupplier(Supplier<T> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @ParametricNullness
        public T get() {
            Supplier<T> supplier = this.delegate;
            synchronized (supplier) {
                return this.delegate.get();
            }
        }

        public String toString() {
            return "Suppliers.synchronizedSupplier(" + this.delegate + ")";
        }
    }

    private static enum SupplierFunctionImpl implements SupplierFunction<Object>
    {
        INSTANCE;


        @Override
        public @Nullable Object apply(Supplier<@Nullable Object> input) {
            return input.get();
        }

        public String toString() {
            return "Suppliers.supplierFunction()";
        }
    }

    private static interface SupplierFunction<T>
    extends Function<Supplier<T>, T> {
    }
}

