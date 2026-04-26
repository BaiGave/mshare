/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={JukeboxBlockEntity.class})
public abstract class JukeboxBlockEntityMixin
implements SpecialLogicContainer {
    @Shadow
    private ItemStack item;
    @Unique
    private boolean fabric_suppressSpecialLogic = false;

    @Shadow
    public abstract void setTheItem(ItemStack var1);

    @Override
    public void fabric_setSuppress(boolean suppress) {
        this.fabric_suppressSpecialLogic = suppress;
    }

    @Inject(method={"setTheItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void setStackBypass(ItemStack stack, CallbackInfo ci) {
        if (this.fabric_suppressSpecialLogic) {
            this.item = stack;
            ci.cancel();
        }
    }

    @Override
    public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
        this.setTheItem(newStack);
    }
}

