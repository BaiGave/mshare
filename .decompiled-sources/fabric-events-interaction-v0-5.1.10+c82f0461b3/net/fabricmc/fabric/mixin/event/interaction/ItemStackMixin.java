/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemStack.class})
public class ItemStackMixin {
    @WrapOperation(method={"use"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")})
    private InteractionResult handleUseEvent(Item instance, Level level, Player player, InteractionHand interactionHand, Operation<InteractionResult> original) {
        InteractionResult result = ItemEvents.USE.invoker().use(level, player, interactionHand);
        if (result != null) {
            return result;
        }
        return original.call(new Object[]{instance, level, player, interactionHand});
    }

    @WrapOperation(method={"useOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;")})
    private InteractionResult handleUseOnEvent(Item instance, UseOnContext useOnContext, Operation<InteractionResult> original) {
        InteractionResult result = ItemEvents.USE_ON.invoker().useOn(useOnContext);
        if (result != null) {
            return result;
        }
        return original.call(instance, useOnContext);
    }
}

