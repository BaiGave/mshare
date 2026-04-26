/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={SimpleContainer.class})
public class SimpleContainerMixin
implements SpecialLogicContainer {
    @Unique
    private boolean fabric_suppressSpecialLogic = false;

    @Redirect(at=@At(value="INVOKE", target="Lnet/minecraft/world/SimpleContainer;setChanged()V"), method={"setItem(ILnet/minecraft/world/item/ItemStack;)V"})
    public void fabric_redirectChanged(SimpleContainer self) {
        if (!this.fabric_suppressSpecialLogic) {
            self.setChanged();
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

