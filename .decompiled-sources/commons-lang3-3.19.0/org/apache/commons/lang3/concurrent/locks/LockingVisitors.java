/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent.locks;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.AbstractSupplier;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.Suppliers;

public class LockingVisitors {
    public static <O> ReadWriteLockVisitor<O> create(O object, ReadWriteLock readWriteLock) {
        return new ReadWriteLockVisitor<O>(object, readWriteLock);
    }

    public static <O> ReentrantLockVisitor<O> create(O object, ReentrantLock reentrantLock) {
        return new ReentrantLockVisitor<O>(object, reentrantLock);
    }

    public static <O> ReentrantLockVisitor<O> reentrantLockVisitor(O object) {
        return LockingVisitors.create(object, new ReentrantLock());
    }

    public static <O> ReadWriteLockVisitor<O> reentrantReadWriteLockVisitor(O object) {
        return LockingVisitors.create(object, new ReentrantReadWriteLock());
    }

    public static <O> StampedLockVisitor<O> stampedLockVisitor(O object) {
        return new StampedLockVisitor<O>(object, new StampedLock());
    }

    @Deprecated
    public LockingVisitors() {
    }

    public static class ReadWriteLockVisitor<O>
    extends LockVisitor<O, ReadWriteLock> {
        public static <O> Builder<O> builder() {
            return new Builder();
        }

        private ReadWriteLockVisitor(Builder<O> builder) {
            super(builder);
        }

        protected ReadWriteLockVisitor(O object, ReadWriteLock readWriteLock) {
            super(object, readWriteLock, readWriteLock::readLock, readWriteLock::writeLock);
        }

        public static class Builder<O>
        extends LockVisitor.LVBuilder<O, ReadWriteLock, Builder<O>> {
            @Override
            public ReadWriteLockVisitor<O> get() {
                return new ReadWriteLockVisitor(this);
            }

            @Override
            public Builder<O> setLock(ReadWriteLock readWriteLock) {
                this.setReadLockSupplier(readWriteLock::readLock);
                this.setWriteLockSupplier(readWriteLock::writeLock);
                return (Builder)super.setLock(readWriteLock);
            }
        }
    }

    public static class ReentrantLockVisitor<O>
    extends LockVisitor<O, ReentrantLock> {
        public static <O> Builder<O> builder() {
            return new Builder();
        }

        private ReentrantLockVisitor(Builder<O> builder) {
            super(builder);
        }

        protected ReentrantLockVisitor(O object, ReentrantLock reentrantLock) {
            super(object, reentrantLock, () -> reentrantLock, () -> reentrantLock);
        }

        public static class Builder<O>
        extends LockVisitor.LVBuilder<O, ReentrantLock, Builder<O>> {
            @Override
            public ReentrantLockVisitor<O> get() {
                return new ReentrantLockVisitor(this);
            }

            @Override
            public Builder<O> setLock(ReentrantLock reentrantLock) {
                this.setReadLockSupplier(() -> reentrantLock);
                this.setWriteLockSupplier(() -> reentrantLock);
                return (Builder)super.setLock(reentrantLock);
            }
        }
    }

    public static class StampedLockVisitor<O>
    extends LockVisitor<O, StampedLock> {
        public static <O> Builder<O> builder() {
            return new Builder();
        }

        private StampedLockVisitor(Builder<O> builder) {
            super(builder);
        }

        protected StampedLockVisitor(O object, StampedLock stampedLock) {
            super(object, stampedLock, stampedLock::asReadLock, stampedLock::asWriteLock);
        }

        public static class Builder<O>
        extends LockVisitor.LVBuilder<O, StampedLock, Builder<O>> {
            @Override
            public StampedLockVisitor<O> get() {
                return new StampedLockVisitor(this);
            }

            @Override
            public Builder<O> setLock(StampedLock stampedLock) {
                this.setReadLockSupplier(stampedLock::asReadLock);
                this.setWriteLockSupplier(stampedLock::asWriteLock);
                return (Builder)super.setLock(stampedLock);
            }
        }
    }

    public static class LockVisitor<O, L> {
        private final L lock;
        private final O object;
        private final Supplier<Lock> readLockSupplier;
        private final Supplier<Lock> writeLockSupplier;

        private LockVisitor(LVBuilder<O, L, ?> builder) {
            this.object = Objects.requireNonNull(builder.object, "object");
            this.lock = Objects.requireNonNull(builder.lock, "lock");
            this.readLockSupplier = Objects.requireNonNull(((LVBuilder)builder).readLockSupplier, "readLockSupplier");
            this.writeLockSupplier = Objects.requireNonNull(((LVBuilder)builder).writeLockSupplier, "writeLockSupplier");
        }

        protected LockVisitor(O object, L lock, Supplier<Lock> readLockSupplier, Supplier<Lock> writeLockSupplier) {
            this.object = Objects.requireNonNull(object, "object");
            this.lock = Objects.requireNonNull(lock, "lock");
            this.readLockSupplier = Objects.requireNonNull(readLockSupplier, "readLockSupplier");
            this.writeLockSupplier = Objects.requireNonNull(writeLockSupplier, "writeLockSupplier");
        }

        public void acceptReadLocked(FailableConsumer<O, ?> consumer) {
            this.lockAcceptUnlock(this.readLockSupplier, consumer);
        }

        public void acceptWriteLocked(FailableConsumer<O, ?> consumer) {
            this.lockAcceptUnlock(this.writeLockSupplier, consumer);
        }

        public <T> T applyReadLocked(FailableFunction<O, T, ?> function) {
            return this.lockApplyUnlock(this.readLockSupplier, function);
        }

        public <T> T applyWriteLocked(FailableFunction<O, T, ?> function) {
            return this.lockApplyUnlock(this.writeLockSupplier, function);
        }

        public L getLock() {
            return this.lock;
        }

        public O getObject() {
            return this.object;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected void lockAcceptUnlock(Supplier<Lock> lockSupplier, FailableConsumer<O, ?> consumer) {
            Lock lock = Objects.requireNonNull(Suppliers.get(lockSupplier), "lock");
            lock.lock();
            try {
                Failable.accept(consumer, this.object);
            }
            finally {
                lock.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected <T> T lockApplyUnlock(Supplier<Lock> lockSupplier, FailableFunction<O, T, ?> function) {
            Lock lock = Objects.requireNonNull(Suppliers.get(lockSupplier), "lock");
            lock.lock();
            try {
                T t = Failable.apply(function, this.object);
                return t;
            }
            finally {
                lock.unlock();
            }
        }

        public static class LVBuilder<O, L, B extends LVBuilder<O, L, B>>
        extends AbstractSupplier<LockVisitor<O, L>, B, RuntimeException> {
            L lock;
            O object;
            private Supplier<Lock> readLockSupplier;
            private Supplier<Lock> writeLockSupplier;

            @Override
            public LockVisitor<O, L> get() {
                return new LockVisitor(this);
            }

            Supplier<Lock> getReadLockSupplier() {
                return this.readLockSupplier;
            }

            Supplier<Lock> getWriteLockSupplier() {
                return this.writeLockSupplier;
            }

            public B setLock(L lock) {
                this.lock = lock;
                return (B)((LVBuilder)this.asThis());
            }

            public B setObject(O object) {
                this.object = object;
                return (B)((LVBuilder)this.asThis());
            }

            public B setReadLockSupplier(Supplier<Lock> readLockSupplier) {
                this.readLockSupplier = readLockSupplier;
                return (B)((LVBuilder)this.asThis());
            }

            public B setWriteLockSupplier(Supplier<Lock> writeLockSupplier) {
                this.writeLockSupplier = writeLockSupplier;
                return (B)((LVBuilder)this.asThis());
            }
        }
    }
}

