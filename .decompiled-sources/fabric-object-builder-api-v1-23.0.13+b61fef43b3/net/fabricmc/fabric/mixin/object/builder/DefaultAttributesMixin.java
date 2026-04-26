/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DefaultAttributes.class})
public abstract class DefaultAttributesMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS;

    @Inject(method={"<clinit>*"}, at={@At(value="TAIL")})
    private static void injectAttributes(CallbackInfo ci) {
        SUPPLIERS = new IdentityHashMap<EntityType<? extends LivingEntity>, AttributeSupplier>(SUPPLIERS);
    }
}

