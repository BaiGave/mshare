/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChiseledBookShelfBlockEntity.class})
public class ChiseledBookShelfBlockEntityMixin
implements SpecialLogicContainer {
    @Shadow
    @Final
    private NonNullList<ItemStack> items;
    @Shadow
    private int lastInteractedSlot;
    @Unique
    private boolean fabric_suppressSpecialLogic = false;
    @Unique
    private final SnapshotParticipant<Integer> fabric_lastInteractedParticipant = new SnapshotParticipant<Integer>(this){
        final /* synthetic */ ChiseledBookShelfBlockEntityMixin this$0;
        {
            ChiseledBookShelfBlockEntityMixin chiseledBookShelfBlockEntityMixin = this$0;
            Objects.requireNonNull(chiseledBookShelfBlockEntityMixin);
            this.this$0 = chiseledBookShelfBlockEntityMixin;
        }

        @Override
        protected Integer createSnapshot() {
            return this.this$0.lastInteractedSlot;
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            this.this$0.lastInteractedSlot = snapshot;
        }

        @Override
        protected void onFinalCommit() {
            this.this$0.updateState(this.this$0.lastInteractedSlot);
        }
    };

    @Override
    public void fabric_setSuppress(boolean suppress) {
        this.fabric_suppressSpecialLogic = suppress;
    }

    @Inject(at={@At(value="HEAD")}, method={"setItem"}, cancellable=true)
    public void setStackBypass(int slot, ItemStack stack, CallbackInfo ci) {
        if (this.fabric_suppressSpecialLogic) {
            this.items.set(slot, stack);
            ci.cancel();
        }
    }

    @Shadow
    private void updateState(int interactedSlot) {
        throw new AssertionError();
    }

    @Override
    public void fabric_onTransfer(int slot, TransactionContext transaction) {
        this.fabric_lastInteractedParticipant.updateSnapshots(transaction);
        this.lastInteractedSlot = slot;
    }

    @Override
    public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
    }
}

