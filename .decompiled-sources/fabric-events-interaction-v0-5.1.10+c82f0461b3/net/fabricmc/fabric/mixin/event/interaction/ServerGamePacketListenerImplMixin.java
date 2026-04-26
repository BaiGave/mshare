/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerGamePacketListenerImpl.class})
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow
    private void tryPickItem(ItemStack stack) {
        throw new AssertionError();
    }

    @WrapOperation(method={"handlePickItemFromBlock"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;")})
    public ItemStack onPickItemFromBlock(BlockState state, LevelReader level, BlockPos pos, boolean includeData, Operation<ItemStack> operation, @Local(argsOnly=true) ServerboundPickItemFromBlockPacket packet) {
        ItemStack stack = PlayerPickItemEvents.BLOCK.invoker().onPickItemFromBlock(this.player, pos, state, packet.includeData());
        if (stack == null) {
            return operation.call(state, level, pos, includeData);
        }
        if (!stack.isEmpty()) {
            this.tryPickItem(stack);
        }
        return ItemStack.EMPTY;
    }

    @WrapOperation(method={"handlePickItemFromEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;")})
    public ItemStack onPickItemFromEntity(Entity entity, Operation<ItemStack> operation, @Local(argsOnly=true) ServerboundPickItemFromEntityPacket packet) {
        ItemStack stack = PlayerPickItemEvents.ENTITY.invoker().onPickItemFromEntity(this.player, entity, packet.includeData());
        if (stack == null) {
            return operation.call(entity);
        }
        if (!stack.isEmpty()) {
            this.tryPickItem(stack);
        }
        return ItemStack.EMPTY;
    }

    @Inject(method={"handleInteract"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;")}, cancellable=true)
    public void handleInteract(ServerboundInteractPacket packet, CallbackInfo info, @Local(name={"target"}) Entity target) {
        ServerLevel level = this.player.level();
        EntityHitResult hitResult = new EntityHitResult(target, packet.location().add(target.getX(), target.getY(), target.getZ()));
        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(this.player, level, packet.hand(), target, hitResult);
        if (result != InteractionResult.PASS) {
            info.cancel();
        }
    }
}

