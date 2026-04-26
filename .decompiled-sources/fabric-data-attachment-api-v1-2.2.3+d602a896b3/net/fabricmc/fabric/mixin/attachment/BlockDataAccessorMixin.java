/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.attachment.DataAccessorHandler;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockDataAccessor.class})
public abstract class BlockDataAccessorMixin
implements DataAccessor {
    @WrapOperation(method={"setData"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V")})
    public void setData(BlockEntity entity, ValueInput input, Operation<Void> original) {
        if (entity.getLevel() == null) {
            original.call(entity, input);
            return;
        }
        DataAccessorHandler.applyDataChanges(entity, input, () -> original.call(entity, input));
    }
}

