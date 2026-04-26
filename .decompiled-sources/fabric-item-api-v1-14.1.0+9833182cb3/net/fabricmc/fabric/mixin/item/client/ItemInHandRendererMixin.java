/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void modifyProgressAnimation(CallbackInfo ci) {
        ItemStack newMainStack = this.minecraft.player.getMainHandItem();
        if (this.mainHandItem.getItem() == newMainStack.getItem() && !this.mainHandItem.getItem().allowComponentsUpdateAnimation(this.minecraft.player, InteractionHand.MAIN_HAND, this.mainHandItem, newMainStack)) {
            this.mainHandItem = newMainStack;
        }
        ItemStack newOffStack = this.minecraft.player.getOffhandItem();
        if (this.offHandItem.getItem() == newOffStack.getItem() && !this.offHandItem.getItem().allowComponentsUpdateAnimation(this.minecraft.player, InteractionHand.OFF_HAND, this.offHandItem, newOffStack)) {
            this.offHandItem = newOffStack;
        }
    }
}

