/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicAccess;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ShelfBlockEntity.class})
public abstract class ShelfBlockEntityMixin
implements SpecialLogicContainer,
SpecialLogicAccess {
    @Unique
    boolean fabric_suppressSpecialLogic;

    @Override
    public void fabric_setSuppress(boolean suppress) {
        this.fabric_suppressSpecialLogic = suppress;
    }

    @Override
    public boolean fabric_shouldSuppressSpecialLogic() {
        return this.fabric_suppressSpecialLogic;
    }

    @Override
    public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
    }
}

