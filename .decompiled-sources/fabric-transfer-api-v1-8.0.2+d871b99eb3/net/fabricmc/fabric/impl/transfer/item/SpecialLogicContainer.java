/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public interface SpecialLogicContainer {
    public void fabric_setSuppress(boolean var1);

    public void fabric_onFinalCommit(int var1, ItemStack var2, ItemStack var3);

    default public void fabric_onTransfer(int slot, TransactionContext transaction) {
    }
}

