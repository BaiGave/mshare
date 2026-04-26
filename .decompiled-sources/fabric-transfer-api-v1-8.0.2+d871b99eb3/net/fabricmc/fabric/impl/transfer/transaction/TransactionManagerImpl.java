/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.transaction;

import java.util.ArrayList;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TransactionManagerImpl {
    public static final ThreadLocal<TransactionManagerImpl> MANAGERS = ThreadLocal.withInitial(TransactionManagerImpl::new);
    private final Thread thread = Thread.currentThread();
    private final ArrayList<TransactionImpl> stack = new ArrayList();
    private final ArrayList<TransactionContext.OuterCloseCallback> outerCloseCallbacks = new ArrayList();
    private int currentDepth = -1;

    public boolean isOpen() {
        return this.currentDepth > -1;
    }

    public Transaction openOuter() {
        if (this.isOpen()) {
            throw new IllegalStateException("An outer transaction is already active on this thread.");
        }
        return this.open();
    }

    public @Nullable TransactionContext getCurrentUnsafe() {
        if (this.currentDepth == -1) {
            return null;
        }
        if (this.stack.get((int)this.currentDepth).lifecycle == Transaction.Lifecycle.OPEN) {
            return this.stack.get(this.currentDepth);
        }
        throw new IllegalStateException("May not call getCurrentUnsafe() from a close callback.");
    }

    Transaction open() {
        ++this.currentDepth;
        if (this.stack.size() == this.currentDepth) {
            this.stack.add(new TransactionImpl(this, this.currentDepth));
        }
        TransactionImpl current = this.stack.get(this.currentDepth);
        current.lifecycle = Transaction.Lifecycle.OPEN;
        return current;
    }

    void validateCurrentThread() {
        if (Thread.currentThread() != this.thread) {
            String errorMessage = String.format("Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.", Thread.currentThread().getName(), this.thread.getName());
            throw new IllegalStateException(errorMessage);
        }
    }

    public Transaction.Lifecycle getLifecycle() {
        if (this.currentDepth == -1) {
            return Transaction.Lifecycle.NONE;
        }
        return this.stack.get((int)this.currentDepth).lifecycle;
    }

    private class TransactionImpl
    implements Transaction {
        final int nestingDepth;
        final ArrayList<TransactionContext.CloseCallback> closeCallbacks;
        Transaction.Lifecycle lifecycle;
        final /* synthetic */ TransactionManagerImpl this$0;

        TransactionImpl(TransactionManagerImpl transactionManagerImpl, int nestingDepth) {
            TransactionManagerImpl transactionManagerImpl2 = transactionManagerImpl;
            Objects.requireNonNull(transactionManagerImpl2);
            this.this$0 = transactionManagerImpl2;
            this.closeCallbacks = new ArrayList();
            this.lifecycle = Transaction.Lifecycle.NONE;
            this.nestingDepth = nestingDepth;
        }

        void validateCurrentTransaction() {
            this.this$0.validateCurrentThread();
            if (this.this$0.currentDepth == -1 || this.this$0.stack.get(this.this$0.currentDepth) != this) {
                String errorMessage = String.format("Transaction function was called on a transaction with depth %d, but the current transaction has depth %d.", this.nestingDepth, this.this$0.currentDepth);
                throw new IllegalStateException(errorMessage);
            }
        }

        private void validateOpen() {
            if (this.lifecycle != Transaction.Lifecycle.OPEN) {
                throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
            }
        }

        @Override
        public Transaction openNested() {
            this.validateCurrentTransaction();
            this.validateOpen();
            return this.this$0.open();
        }

        private void close(TransactionContext.Result result) {
            int i;
            this.validateCurrentTransaction();
            this.validateOpen();
            this.lifecycle = Transaction.Lifecycle.CLOSING;
            Throwable closeException = null;
            for (i = this.closeCallbacks.size() - 1; i >= 0; --i) {
                try {
                    this.closeCallbacks.get(i).onClose(this, result);
                    continue;
                }
                catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
                        continue;
                    }
                    closeException.addSuppressed(exception);
                }
            }
            this.closeCallbacks.clear();
            if (this.this$0.currentDepth == 0) {
                this.lifecycle = Transaction.Lifecycle.OUTER_CLOSING;
                for (i = this.this$0.outerCloseCallbacks.size() - 1; i >= 0; --i) {
                    try {
                        this.this$0.outerCloseCallbacks.get(i).afterOuterClose(result);
                        continue;
                    }
                    catch (Exception exception) {
                        if (closeException == null) {
                            closeException = new RuntimeException("Encountered an exception while invoking a transaction outer close callback.", exception);
                            continue;
                        }
                        closeException.addSuppressed(exception);
                    }
                }
                this.this$0.outerCloseCallbacks.clear();
            }
            --this.this$0.currentDepth;
            this.lifecycle = Transaction.Lifecycle.NONE;
            if (closeException != null) {
                throw closeException;
            }
        }

        @Override
        public void abort() {
            this.close(TransactionContext.Result.ABORTED);
        }

        @Override
        public void commit() {
            this.close(TransactionContext.Result.COMMITTED);
        }

        @Override
        public void close() {
            if (this.this$0.isOpen() && this.lifecycle == Transaction.Lifecycle.OPEN) {
                this.abort();
            }
        }

        @Override
        public int nestingDepth() {
            this.this$0.validateCurrentThread();
            return this.nestingDepth;
        }

        @Override
        public Transaction getOpenTransaction(int nestingDepth) {
            this.this$0.validateCurrentThread();
            if (nestingDepth < 0) {
                throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
            }
            if (nestingDepth > this.this$0.currentDepth) {
                throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);
            }
            TransactionImpl transaction = this.this$0.stack.get(nestingDepth);
            transaction.validateOpen();
            return transaction;
        }

        @Override
        public void addCloseCallback(TransactionContext.CloseCallback closeCallback) {
            this.this$0.validateCurrentThread();
            this.validateOpen();
            this.closeCallbacks.add(closeCallback);
        }

        @Override
        public void addOuterCloseCallback(TransactionContext.OuterCloseCallback outerCloseCallback) {
            this.this$0.validateCurrentThread();
            if (this.this$0.currentDepth == -1) {
                throw new IllegalStateException("There is no open transaction on this thread.");
            }
            this.this$0.outerCloseCallbacks.add(outerCloseCallback);
        }

        public String toString() {
            return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(this.nestingDepth, this.lifecycle.name(), this.this$0.thread.getName());
        }
    }
}

