/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import java.util.Objects;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityType.Builder.class})
public abstract class EntityTypeBuilderMixin<T extends Entity>
implements FabricEntityType.Builder<T>,
FabricEntityTypeImpl.Builder {
    @Unique
    private @Nullable Boolean alwaysUpdateVelocity = null;
    @Unique
    private @Nullable Boolean canPotentiallyExecuteCommands = null;
    @Unique
    private FabricEntityTypeImpl.Builder.Living<? extends LivingEntity> livingBuilder = null;
    @Unique
    private FabricEntityTypeImpl.Builder.Mob<? extends Mob> mobBuilder = null;

    @Shadow
    public abstract EntityType<T> build(ResourceKey<EntityType<?>> var1);

    @Override
    public EntityType.Builder<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
        this.alwaysUpdateVelocity = alwaysUpdateVelocity;
        return (EntityType.Builder)((Object)this);
    }

    @Override
    public EntityType.Builder<T> canPotentiallyExecuteCommands(boolean canPotentiallyExecuteCommands) {
        this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
        return (EntityType.Builder)((Object)this);
    }

    @Inject(method={"build"}, at={@At(value="RETURN")})
    private void applyChildBuilders(ResourceKey<EntityType<?>> resourceKey, CallbackInfoReturnable<EntityType<T>> cir) {
        EntityType<T> entityType = cir.getReturnValue();
        if (!(entityType instanceof FabricEntityTypeImpl)) {
            throw new IllegalStateException();
        }
        FabricEntityTypeImpl entityType2 = (FabricEntityTypeImpl)((Object)entityType);
        entityType2.fabric_setAlwaysUpdateVelocity(this.alwaysUpdateVelocity);
        entityType2.fabric_setCanPotentiallyExecuteCommands(this.canPotentiallyExecuteCommands);
        if (this.livingBuilder != null) {
            this.livingBuilder.onBuild(EntityTypeBuilderMixin.castLiving(cir.getReturnValue()));
        }
        if (this.mobBuilder != null) {
            this.mobBuilder.onBuild(EntityTypeBuilderMixin.castMob(cir.getReturnValue()));
        }
    }

    @Unique
    private static <T extends LivingEntity> EntityType<T> castLiving(EntityType<?> type) {
        return type;
    }

    @Unique
    private static <T extends Mob> EntityType<T> castMob(EntityType<?> type) {
        return type;
    }

    @WrapOperation(method={"build"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Util;fetchChoiceType(Lcom/mojang/datafixers/DSL$TypeReference;Ljava/lang/String;)Lcom/mojang/datafixers/types/Type;")})
    private @Nullable Type<?> allowNoModdedDatafixers(DSL.TypeReference typeReference, String id, Operation<Type<?>> original, @Local(argsOnly=true) ResourceKey<EntityType<?>> resourceKey) {
        if (!resourceKey.identifier().getNamespace().equals("minecraft")) {
            return null;
        }
        return original.call(typeReference, id);
    }

    @Override
    public void fabric_setLivingEntityBuilder(FabricEntityTypeImpl.Builder.Living<? extends LivingEntity> livingBuilder) {
        Objects.requireNonNull(livingBuilder, "Cannot set null living entity builder");
        this.livingBuilder = livingBuilder;
    }

    @Override
    public void fabric_setMobEntityBuilder(FabricEntityTypeImpl.Builder.Mob<? extends Mob> mobBuilder) {
        Objects.requireNonNull(mobBuilder, "Cannot set null mob entity builder");
        this.mobBuilder = mobBuilder;
    }
}

