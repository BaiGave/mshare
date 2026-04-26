/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class StorageUtil {
    private StorageUtil() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> long move(@Nullable Storage<T> from, @Nullable Storage<T> to, Predicate<T> filter, long maxAmount, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null) return 0L;
        if (to == null) {
            return 0L;
        }
        long totalMoved = 0L;
        try (Transaction iterationTransaction = Transaction.openNested(transaction);){
            for (StorageView<T> view : from.nonEmptyViews()) {
                T resource = view.getResource();
                if (!filter.test(resource)) continue;
                long maxExtracted = StorageUtil.simulateExtract(view, resource, maxAmount - totalMoved, (TransactionContext)iterationTransaction);
                try (Transaction transferTransaction = iterationTransaction.openNested();){
                    long accepted = to.insert(resource, maxExtracted, transferTransaction);
                    if (view.extract(resource, accepted, transferTransaction) == accepted) {
                        totalMoved += accepted;
                        transferTransaction.commit();
                    }
                }
                if (maxAmount != totalMoved) continue;
                iterationTransaction.commit();
                long l = totalMoved;
                return l;
            }
            iterationTransaction.commit();
            return totalMoved;
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
            report.addCategory("Move details").setDetail("Input storage", (CrashReportDetail)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)(from)).setDetail("Output storage", (CrashReportDetail)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)(to)).setDetail("Filter", (CrashReportDetail)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)(filter)).setDetail("Max amount", maxAmount).setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    public static <T> long simulateInsert(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction);){
            long l = storage.insert(resource, maxAmount, simulateTransaction);
            return l;
        }
    }

    public static <T> long simulateExtract(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction);){
            long l = storage.extract(resource, maxAmount, simulateTransaction);
            return l;
        }
    }

    public static <T> long simulateExtract(StorageView<T> storageView, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction);){
            long l = storageView.extract(resource, maxAmount, simulateTransaction);
            return l;
        }
    }

    public static <T, S extends Object & StorageView<T>> long simulateExtract(S storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction);){
            long l = ((StorageView<T>)storage).extract(resource, maxAmount, simulateTransaction);
            return l;
        }
    }

    public static <T> @Nullable ResourceAmount<T> extractAny(@Nullable Storage<T> storage, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        if (storage == null) {
            return null;
        }
        try {
            for (StorageView<T> view : storage.nonEmptyViews()) {
                T resource;
                long amount = view.extract(resource = view.getResource(), maxAmount, transaction);
                if (amount <= 0L) continue;
                return new ResourceAmount<T>(resource, amount);
            }
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Extracting resources from storage");
            report.addCategory("Extraction details").setDetail("Storage", (CrashReportDetail)LambdaMetafactory.metafactory(null, null, null, ()Ljava/lang/Object;, toString(), ()Ljava/lang/String;)(storage)).setDetail("Max amount", maxAmount).setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
        return null;
    }

    public static <T> long insertStacking(List<? extends SingleSlotStorage<T>> slots, T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        long amount = 0L;
        try {
            for (SingleSlotStorage<T> slot : slots) {
                if (slot.isResourceBlank() || (amount += slot.insert(resource, maxAmount - amount, transaction)) != maxAmount) continue;
                return amount;
            }
            for (SingleSlotStorage<T> slot : slots) {
                if ((amount += slot.insert(resource, maxAmount - amount, transaction)) != maxAmount) continue;
                return amount;
            }
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Inserting resources into slots");
            report.addCategory("Slotted insertion details").setDetail("Slots", () -> Objects.toString(slots, null)).setDetail("Resource", () -> Objects.toString(resource, null)).setDetail("Max amount", maxAmount).setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
        return amount;
    }

    public static <T> long tryInsertStacking(@Nullable Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        try {
            if (storage instanceof SlottedStorage) {
                SlottedStorage slottedStorage = (SlottedStorage)storage;
                return StorageUtil.insertStacking(slottedStorage.getSlots(), resource, maxAmount, transaction);
            }
            if (storage != null) {
                return storage.insert(resource, maxAmount, transaction);
            }
            return 0L;
        }
        catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Inserting resources into a storage");
            report.addCategory("Insertion details").setDetail("Storage", () -> Objects.toString(storage, null)).setDetail("Resource", () -> Objects.toString(resource, null)).setDetail("Max amount", maxAmount).setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    public static <T> @Nullable T findStoredResource(@Nullable Storage<T> storage) {
        return (T)StorageUtil.findStoredResource(storage, r -> true);
    }

    public static <T> @Nullable T findStoredResource(@Nullable Storage<T> storage, Predicate<T> filter) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (storage == null) {
            return null;
        }
        for (StorageView<T> view : storage.nonEmptyViews()) {
            if (!filter.test(view.getResource())) continue;
            return view.getResource();
        }
        return null;
    }

    public static <T> @Nullable T findExtractableResource(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
        return (T)StorageUtil.findExtractableResource(storage, r -> true, transaction);
    }

    public static <T> @Nullable T findExtractableResource(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (storage == null) {
            return null;
        }
        try (Transaction nested = Transaction.openNested(transaction);){
            for (StorageView<T> view : storage.nonEmptyViews()) {
                T resource = view.getResource();
                if (!filter.test(resource) || view.extract(resource, Long.MAX_VALUE, nested) <= 0L) continue;
                T t = resource;
                return t;
            }
        }
        return null;
    }

    public static <T> @Nullable ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
        return StorageUtil.findExtractableContent(storage, r -> true, transaction);
    }

    public static <T> @Nullable ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
        long extractableAmount;
        T extractableResource = StorageUtil.findExtractableResource(storage, filter, transaction);
        if (extractableResource != null && (extractableAmount = StorageUtil.simulateExtract(storage, extractableResource, Long.MAX_VALUE, transaction)) > 0L) {
            return new ResourceAmount<T>(extractableResource, extractableAmount);
        }
        return null;
    }

    public static <T> int getRedstoneSignal(@Nullable Storage<T> storage) {
        if (storage == null) {
            return 0;
        }
        double fillPercentage = 0.0;
        int viewCount = 0;
        boolean hasNonEmptyView = false;
        for (StorageView<T> view : storage) {
            ++viewCount;
            if (view.getAmount() <= 0L) continue;
            fillPercentage += (double)view.getAmount() / (double)view.getCapacity();
            hasNonEmptyView = true;
        }
        return Mth.floor(fillPercentage / (double)viewCount * 14.0) + (hasNonEmptyView ? 1 : 0);
    }
}

