/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BrewingStandBlockEntity.class})
public class BrewingStandBlockEntityMixin {
    @Redirect(method={"doBrew"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;getCraftingRemainder()Lnet/minecraft/world/item/ItemStackTemplate;"))
    private static ItemStackTemplate getCraftingRemainder(Item item, @Local(name={"ingredient"}) ItemStack ingredient) {
        return ingredient.getCraftingRemainder();
    }
}

