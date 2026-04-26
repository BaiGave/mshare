/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.transaction.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public abstract class SnapshotParticipant<T>
implements TransactionContext.CloseCallback,
TransactionContext.OuterCloseCallback {
    private final List<T> snapshots = new ArrayList<T>();

    protected abstract T createSnapshot();

    protected abstract void readSnapshot(T var1);

    protected void releaseSnapshot(T snapshot) {
    }

    protected void onFinalCommit() {
    }

    public void updateSnapshots(TransactionContext transaction) {
        while (this.snapshots.size() <= transaction.nestingDepth()) {
            this.snapshots.add(null);
        }
        if (this.snapshots.get(transaction.nestingDepth()) == null) {
            T snapshot = this.createSnapshot();
            Objects.requireNonNull(snapshot, "Snapshot may not be null!");
            this.snapshots.set(transaction.nestingDepth(), snapshot);
            transaction.addCloseCallback(this);
        }
    }

    @Override
    public void onClose(TransactionContext transaction, TransactionContext.Result result) {
        Object snapshot = this.snapshots.set(transaction.nestingDepth(), null);
        if (result.wasAborted()) {
            this.readSnapshot(snapshot);
            this.releaseSnapshot(snapshot);
        } else if (transaction.nestingDepth() > 0) {
            if (this.snapshots.get(transaction.nestingDepth() - 1) == null) {
                this.snapshots.set(transaction.nestingDepth() - 1, snapshot);
                transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
            } else {
                this.releaseSnapshot(snapshot);
            }
        } else {
            this.releaseSnapshot(snapshot);
            transaction.addOuterCloseCallback(this);
        }
    }

    @Override
    public void afterOuterClose(TransactionContext.Result result) {
        this.onFinalCommit();
    }
}

