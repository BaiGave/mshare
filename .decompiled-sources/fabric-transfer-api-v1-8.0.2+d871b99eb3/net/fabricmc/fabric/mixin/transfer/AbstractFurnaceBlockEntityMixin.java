/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractFurnaceBlockEntity.class})
public abstract class AbstractFurnaceBlockEntityMixin
extends BaseContainerBlockEntity
implements SpecialLogicContainer {
    @Shadow
    protected NonNullList<ItemStack> items;
    @Shadow
    private int cookingTimer;
    @Shadow
    private int cookingTotalTime;
    @Unique
    private boolean fabric_suppressSpecialLogic = false;

    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        throw new AssertionError();
    }

    @Inject(at={@At(value="HEAD")}, method={"setItem"}, cancellable=true)
    public void setStackSuppressUpdate(int slot, ItemStack stack, CallbackInfo ci) {
        if (this.fabric_suppressSpecialLogic) {
            this.items.set(slot, stack);
            ci.cancel();
        }
    }

    @Override
    public void fabric_setSuppress(boolean suppress) {
        this.fabric_suppressSpecialLogic = suppress;
    }

    @Override
    public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
        if (slot == 0) {
            Level level;
            boolean bl;
            ItemStack itemStack = oldStack;
            ItemStack stack = newStack;
            boolean bl2 = bl = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack);
            if (!bl && (level = this.level) instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                this.cookingTotalTime = AbstractFurnaceBlockEntityMixin.getTotalCookTime(level2, (AbstractFurnaceBlockEntity)((Object)this));
                this.cookingTimer = 0;
            }
        }
    }

    @Shadow
    private static int getTotalCookTime(ServerLevel level, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
        throw new AssertionError();
    }
}

