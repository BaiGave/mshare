/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item.client;

import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin {
    @Inject(method={"getTooltipLines"}, at={@At(value="RETURN", ordinal=1)})
    private void getTooltip(Item.TooltipContext tooltipContext, @Nullable Player entity, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> info) {
        ItemTooltipCallback.EVENT.invoker().getTooltip((ItemStack)((Object)this), tooltipContext, tooltipFlag, info.getReturnValue());
    }
}

