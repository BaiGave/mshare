/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.impl.item.EnchantmentUtil;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Enchantment.Builder.class})
public class EnchantmentBuilderMixin
implements EnchantmentUtil.BuilderExtensions {
    @Unique
    private boolean didModify = false;

    @Inject(method={"*"}, at={@At(value="RETURN")})
    private void markModified(CallbackInfoReturnable<?> cir) {
        if (cir.getReturnValue() == this) {
            this.didModify = true;
        }
    }

    @Override
    public void fabric$resetModified() {
        this.didModify = false;
    }

    @Override
    public boolean fabric$didModify() {
        return this.didModify;
    }
}

