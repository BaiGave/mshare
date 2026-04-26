/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.message;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class}, priority=800)
public abstract class ClientPacketListenerMixin {
    @Inject(method={"sendChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void fabric_allowSendChatMessage(String _content, CallbackInfo ci, @Local(argsOnly=true) LocalRef<String> content) {
        if (ClientSendMessageEvents.ALLOW_CHAT.invoker().allowSendChatMessage(content.get())) {
            content.set(ClientSendMessageEvents.MODIFY_CHAT.invoker().modifySendChatMessage(content.get()));
            ClientSendMessageEvents.CHAT.invoker().onSendChatMessage(content.get());
        } else {
            ClientSendMessageEvents.CHAT_CANCELED.invoker().onSendChatMessageCanceled(content.get());
            ci.cancel();
        }
    }

    @Inject(method={"sendCommand"}, at={@At(value="HEAD")}, cancellable=true)
    private void fabric_allowSendCommandMessage(String _command, CallbackInfo ci, @Local(argsOnly=true) LocalRef<String> command) {
        if (ClientSendMessageEvents.ALLOW_COMMAND.invoker().allowSendCommandMessage(command.get())) {
            command.set(ClientSendMessageEvents.MODIFY_COMMAND.invoker().modifySendCommandMessage(command.get()));
            ClientSendMessageEvents.COMMAND.invoker().onSendCommandMessage(command.get());
        } else {
            ClientSendMessageEvents.COMMAND_CANCELED.invoker().onSendCommandMessageCanceled(command.get());
            ci.cancel();
        }
    }
}

