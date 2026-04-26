/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachmentsProvider;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
abstract class ClientPacketListenerMixin
implements GlobalAttachmentsProvider {
    @Unique
    private GlobalAttachmentsImpl globalAttachments;

    ClientPacketListenerMixin() {
    }

    @Override
    public GlobalAttachments globalAttachments() {
        return this.globalAttachments;
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void initGlobalAttachments(CallbackInfo ci) {
        this.globalAttachments = new GlobalAttachmentsImpl(null);
    }

    @WrapOperation(method={"handleRespawn"}, at={@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", opcode=181)}, slice={@Slice(from=@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;startWaitingForNewLevel(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/gui/screens/LevelLoadingScreen$Reason;)V"))})
    private void copyAttachmentsOnClientRespawn(Minecraft client, LocalPlayer newPlayer, Operation<Void> init, ClientboundRespawnPacket packet, @Local(name={"oldPlayer"}) LocalPlayer oldPlayer) {
        AttachmentTargetImpl.transfer(oldPlayer, newPlayer, !packet.shouldKeep((byte)1));
        init.call(client, newPlayer);
    }
}

