/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.command.v2;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandBuildContext;

public interface ClientCommandRegistrationCallback {
    public static final Event<ClientCommandRegistrationCallback> EVENT = EventFactory.createArrayBacked(ClientCommandRegistrationCallback.class, callbacks -> (dispatcher, buildContext) -> {
        for (ClientCommandRegistrationCallback callback : callbacks) {
            callback.register(dispatcher, buildContext);
        }
    });

    public void register(CommandDispatcher<FabricClientCommandSource> var1, CommandBuildContext var2);
}

