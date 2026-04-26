/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.mixin.event.interaction.client.KeyMappingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Minecraft.class})
public abstract class MinecraftMixin {
    @Unique
    private boolean attackCancelled;
    @Shadow
    public LocalPlayer player;
    @Shadow
    @Final
    public Options options;
    @Shadow
    public @Nullable MultiPlayerGameMode gameMode;
    @Shadow
    public @Nullable ClientLevel level;

    @Shadow
    public abstract ClientPacketListener getConnection();

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")}, method={"startUseItem"}, cancellable=true)
    private void injectUseEntityCallback(CallbackInfo ci, @Local(name={"hand"}) InteractionHand hand, @Local(name={"entityHit"}) EntityHitResult hitResult, @Local(name={"entity"}) Entity entity) {
        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(this.player, this.player.level(), hand, entity, hitResult);
        if (result != InteractionResult.PASS) {
            InteractionResult.Success success;
            if (result.consumesAction()) {
                Vec3 hitVec = hitResult.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
                this.getConnection().send(new ServerboundInteractPacket(entity.getId(), hand, hitVec, this.player.isShiftKeyDown()));
            }
            if (result instanceof InteractionResult.Success && (success = (InteractionResult.Success)result).swingSource() == InteractionResult.SwingSource.CLIENT) {
                this.player.swing(hand);
            }
            ci.cancel();
        }
    }

    @Inject(method={"handleKeybinds"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal=0)})
    private void injectHandleInputEventsForPreAttackCallback(CallbackInfo ci) {
        int attackKeyPressCount = ((KeyMappingAccessor)((Object)this.options.keyAttack)).fabric_getTimesPressed();
        this.attackCancelled = this.options.keyAttack.isDown() || attackKeyPressCount != 0 ? ClientPreAttackCallback.EVENT.invoker().onClientPlayerPreAttack((Minecraft)((Object)this), this.player, attackKeyPressCount) : false;
    }

    @Inject(method={"startAttack"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectDoAttackForCancelling(CallbackInfoReturnable<Boolean> cir) {
        if (this.attackCancelled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"continueAttack"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectHandleBlockBreakingForCancelling(boolean breaking, CallbackInfo ci) {
        if (this.attackCancelled) {
            if (this.gameMode != null) {
                this.gameMode.stopDestroyBlock();
            }
            ci.cancel();
        }
    }
}

