/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BaseContainerBlockEntity.class})
public class BaseContainerBlockEntityMixin
implements SpecialLogicContainer {
    @Unique
    private boolean fabric_suppressSpecialLogic = false;

    @WrapOperation(at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;setChanged()V")}, method={"setItem(ILnet/minecraft/world/item/ItemStack;)V"})
    public void fabric_redirectSetChanged(BaseContainerBlockEntity instance, Operation<Void> original) {
        if (!this.fabric_suppressSpecialLogic) {
            original.call(instance);
        }
    }

    @Override
    public void fabric_setSuppress(boolean suppress) {
        this.fabric_suppressSpecialLogic = suppress;
    }

    @Override
    public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
    }
}

