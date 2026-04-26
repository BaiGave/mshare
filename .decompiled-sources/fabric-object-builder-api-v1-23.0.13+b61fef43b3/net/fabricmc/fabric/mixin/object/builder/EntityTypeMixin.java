/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityType.class})
public abstract class EntityTypeMixin
implements FabricEntityTypeImpl {
    @Unique
    private @Nullable Boolean alwaysUpdateVelocity;
    @Unique
    private @Nullable Boolean canPotentiallyExecuteCommands;

    @Inject(method={"trackDeltas"}, at={@At(value="HEAD")}, cancellable=true)
    public void onAlwaysUpdateVelocity(CallbackInfoReturnable<Boolean> cir) {
        if (this.alwaysUpdateVelocity != null) {
            cir.setReturnValue(this.alwaysUpdateVelocity);
        }
    }

    @Inject(method={"onlyOpCanSetNbt"}, at={@At(value="HEAD")}, cancellable=true)
    public void onCanPotentiallyExecuteCommands(CallbackInfoReturnable<Boolean> cir) {
        if (this.canPotentiallyExecuteCommands != null) {
            cir.setReturnValue(this.canPotentiallyExecuteCommands);
        }
    }

    @Override
    public void fabric_setAlwaysUpdateVelocity(@Nullable Boolean alwaysUpdateVelocity) {
        this.alwaysUpdateVelocity = alwaysUpdateVelocity;
    }

    @Override
    public void fabric_setCanPotentiallyExecuteCommands(@Nullable Boolean canPotentiallyExecuteCommands) {
        this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
    }
}

