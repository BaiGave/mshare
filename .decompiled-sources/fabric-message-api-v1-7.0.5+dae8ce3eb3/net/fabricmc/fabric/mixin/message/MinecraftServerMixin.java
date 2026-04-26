/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.message;

import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MinecraftServer.class})
public class MinecraftServerMixin {
    @Inject(method={"getChatDecorator"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetChatDecorator(CallbackInfoReturnable<ChatDecorator> cir) {
        cir.setReturnValue((sender, message) -> ServerMessageDecoratorEvent.EVENT.invoker().decorate(sender, message));
    }
}

