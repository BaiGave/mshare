/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicAccess;
import net.minecraft.world.level.block.entity.ListBackedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ListBackedContainer.class})
interface ListBackedContainerMixin
extends ListBackedContainer,
SpecialLogicAccess {
    @WrapOperation(method={"setItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/ListBackedContainer;setChanged()V")})
    private void cancelSetChanged(ListBackedContainer instance, Operation<Void> original) {
        if (!this.fabric_shouldSuppressSpecialLogic()) {
            original.call(instance);
        }
    }
}

