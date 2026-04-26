/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerPlayerGameMode.class})
public class ServerPlayerGameModeMixin {
    @Final
    @Shadow
    protected ServerPlayer player;
    @Shadow
    protected ServerLevel level;

    @Inject(at={@At(value="HEAD")}, method={"handleBlockBreakAction"}, cancellable=true)
    public void startBlockBreak(BlockPos pos, ServerboundPlayerActionPacket.Action playerAction, Direction direction, int worldHeight, int i, CallbackInfo info) {
        if (playerAction != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            return;
        }
        InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(this.player, this.level, InteractionHand.MAIN_HAND, pos, direction);
        if (result != InteractionResult.PASS) {
            Packet<ClientGamePacketListener> updatePacket;
            BlockEntity blockEntity;
            this.player.connection.send(new ClientboundBlockUpdatePacket(this.level, pos));
            if (this.level.getBlockState(pos).hasBlockEntity() && (blockEntity = this.level.getBlockEntity(pos)) != null && (updatePacket = blockEntity.getUpdatePacket()) != null) {
                this.player.connection.send(updatePacket);
            }
            info.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"useItemOn"}, cancellable=true)
    public void interactBlock(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, level, hand, blockHitResult);
        if (result != InteractionResult.PASS) {
            info.setReturnValue(result);
            info.cancel();
            return;
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"useItem"}, cancellable=true)
    public void interactItem(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult result = UseItemCallback.EVENT.invoker().interact(player, level, hand);
        if (result != InteractionResult.PASS) {
            info.setReturnValue(result);
            info.cancel();
            return;
        }
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;")}, method={"destroyBlock"}, cancellable=true)
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(name={"blockEntity"}) BlockEntity blockEntity, @Local(name={"state"}) BlockState state) {
        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(this.level, this.player, pos, state, blockEntity);
        if (!result) {
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(this.level, this.player, pos, state, blockEntity);
            cir.setReturnValue(false);
        }
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")}, method={"destroyBlock"})
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(name={"blockEntity"}) BlockEntity blockEntity, @Local(name={"adjustedState"}) BlockState adjustedState) {
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(this.level, this.player, pos, adjustedState, blockEntity);
    }
}

