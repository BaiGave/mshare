/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TransactionContext {
    public Transaction openNested();

    public int nestingDepth();

    public Transaction getOpenTransaction(int var1);

    public void addCloseCallback(CloseCallback var1);

    public void addOuterCloseCallback(OuterCloseCallback var1);

    public static enum Result {
        ABORTED,
        COMMITTED;


        public boolean wasAborted() {
            return this == ABORTED;
        }

        public boolean wasCommitted() {
            return this == COMMITTED;
        }
    }

    @FunctionalInterface
    public static interface OuterCloseCallback {
        public void afterOuterClose(Result var1);
    }

    @FunctionalInterface
    public static interface CloseCallback {
        public void onClose(TransactionContext var1, Result var2);
    }
}

