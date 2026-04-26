/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.command.v2;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public interface CommandRegistrationCallback {
    public static final Event<CommandRegistrationCallback> EVENT = EventFactory.createArrayBacked(CommandRegistrationCallback.class, callbacks -> (dispatcher, buildContext, selection) -> {
        for (CommandRegistrationCallback callback : callbacks) {
            callback.register(dispatcher, buildContext, selection);
        }
    });

    public void register(CommandDispatcher<CommandSourceStack> var1, CommandBuildContext var2, Commands.CommandSelection var3);
}

