/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private BlockPos destroyBlockPos;
    @Shadow
    private ItemStack destroyingItem;

    @Redirect(at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), method={"sameDestroyTarget"})
    private boolean fabricItemContinueBlockBreakingInject(ItemStack stack, ItemStack otherStack) {
        ItemStack newStack;
        ItemStack oldStack;
        boolean stackUnchanged = ItemStack.isSameItemSameComponents(stack, this.destroyingItem);
        if (!stackUnchanged && (oldStack = this.destroyingItem).is((newStack = this.minecraft.player.getMainHandItem()).getItem()) && oldStack.getItem().allowContinuingBlockBreaking(this.minecraft.player, oldStack, newStack)) {
            stackUnchanged = true;
        }
        return stackUnchanged;
    }
}

