/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.apache.logging.log4j.util.Cast;
import org.apache.logging.log4j.util.Lazy;

final class LazyUtil {
    private static final Object NULL = new Object(){

        public String toString() {
            return "null";
        }
    };

    LazyUtil() {
    }

    static Object wrapNull(Object value) {
        return value == null ? NULL : value;
    }

    static <T> T unwrapNull(Object value) {
        return value == NULL ? null : (T)Cast.cast(value);
    }

    static class PureLazy<T>
    implements Lazy<T> {
        private final Supplier<T> supplier;
        private Object value;

        public PureLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T value() {
            Object value = this.value;
            if (value == null) {
                value = this.supplier.get();
                this.value = LazyUtil.wrapNull(value);
            }
            return LazyUtil.unwrapNull(value);
        }

        @Override
        public boolean isInitialized() {
            return this.value != null;
        }

        @Override
        public void set(T newValue) {
            this.value = newValue;
        }
    }

    static class SafeLazy<T>
    implements Lazy<T> {
        private final Lock lock = new ReentrantLock();
        private final Supplier<T> supplier;
        private volatile Object value;

        SafeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T value() {
            Object value = this.value;
            if (value == null) {
                this.lock.lock();
                try {
                    value = this.value;
                    if (value == null) {
                        value = this.supplier.get();
                        this.value = LazyUtil.wrapNull(value);
                    }
                }
                finally {
                    this.lock.unlock();
                }
            }
            return LazyUtil.unwrapNull(value);
        }

        @Override
        public void set(T newValue) {
            this.value = newValue;
        }

        public void reset() {
            this.value = null;
        }

        @Override
        public boolean isInitialized() {
            return this.value != null;
        }

        public String toString() {
            return this.isInitialized() ? String.valueOf(this.value) : "Lazy value not initialized";
        }
    }

    static class WeakConstant<T>
    implements Lazy<T> {
        private final WeakReference<T> reference;

        WeakConstant(T value) {
            this.reference = new WeakReference<T>(value);
        }

        @Override
        public T value() {
            return this.reference.get();
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public void set(T newValue) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return String.valueOf(this.value());
        }
    }

    static class Constant<T>
    implements Lazy<T> {
        private final T value;

        Constant(T value) {
            this.value = value;
        }

        @Override
        public T value() {
            return this.value;
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public void set(T newValue) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return String.valueOf(this.value);
        }
    }
}

