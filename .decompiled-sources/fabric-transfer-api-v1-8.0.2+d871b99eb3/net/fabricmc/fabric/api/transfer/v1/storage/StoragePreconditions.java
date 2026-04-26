/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.storage;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

public final class StoragePreconditions {
    public static void notBlank(TransferVariant<?> variant) {
        if (variant.isBlank()) {
            throw new IllegalArgumentException("Transfer variant may not be blank.");
        }
    }

    public static void notNegative(long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException("Amount may not be negative, but it is: " + amount);
        }
    }

    public static void notBlankNotNegative(TransferVariant<?> variant, long amount) {
        StoragePreconditions.notBlank(variant);
        StoragePreconditions.notNegative(amount);
    }

    private StoragePreconditions() {
    }
}

