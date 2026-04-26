/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockBehaviour.BlockStateBase.class})
public abstract class BlockBehaviourBlockStateBaseMixin {
    @Shadow
    protected abstract BlockState asState();

    @Inject(method={"useItemOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void callUseItemOnEvent(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BlockEvents.USE_ITEM_ON.invoker().useItemOn(itemStack, this.asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method={"useWithoutItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void callUseWithoutItemEvent(Level level, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BlockEvents.USE_WITHOUT_ITEM.invoker().useWithoutItem(this.asState(), level, blockHitResult.getBlockPos(), player, blockHitResult);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}

