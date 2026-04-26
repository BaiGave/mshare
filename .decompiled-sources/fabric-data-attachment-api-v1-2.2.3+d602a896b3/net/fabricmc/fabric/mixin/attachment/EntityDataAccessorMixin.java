/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.attachment.DataAccessorHandler;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={EntityDataAccessor.class})
public abstract class EntityDataAccessorMixin
implements DataAccessor {
    @WrapOperation(method={"setData"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;load(Lnet/minecraft/world/level/storage/ValueInput;)V")})
    public void setData(Entity entity, ValueInput input, Operation<Void> original) {
        if (entity.level() == null) {
            original.call(entity, input);
            return;
        }
        DataAccessorHandler.applyDataChanges(entity, input, () -> original.call(entity, input));
    }
}

