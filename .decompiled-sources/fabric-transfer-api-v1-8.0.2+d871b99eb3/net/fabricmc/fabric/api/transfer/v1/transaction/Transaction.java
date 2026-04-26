/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.transaction.TransactionManagerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Transaction
extends AutoCloseable,
TransactionContext {
    public static Transaction openOuter() {
        return TransactionManagerImpl.MANAGERS.get().openOuter();
    }

    public static boolean isOpen() {
        return Transaction.getLifecycle() != Lifecycle.NONE;
    }

    public static Lifecycle getLifecycle() {
        return TransactionManagerImpl.MANAGERS.get().getLifecycle();
    }

    public static Transaction openNested(@Nullable TransactionContext maybeParent) {
        return maybeParent == null ? Transaction.openOuter() : maybeParent.openNested();
    }

    @Deprecated
    public static @Nullable TransactionContext getCurrentUnsafe() {
        return TransactionManagerImpl.MANAGERS.get().getCurrentUnsafe();
    }

    public void abort();

    public void commit();

    @Override
    public void close();

    public static enum Lifecycle {
        NONE,
        OPEN,
        CLOSING,
        OUTER_CLOSING;

    }
}

