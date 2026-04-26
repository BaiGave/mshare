/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.timers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public record FunctionCallback(Identifier functionId) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionCallback> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(FunctionCallback::functionId)).apply((Applicative<FunctionCallback, ?>)i, FunctionCallback::new));

    @Override
    public void handle(MinecraftServer server, TimerQueue<MinecraftServer> queue, long time) {
        ServerFunctionManager functionManager = server.getFunctions();
        functionManager.get(this.functionId).ifPresent(function -> functionManager.execute((CommandFunction<CommandSourceStack>)function, functionManager.getGameLoopSender()));
    }

    @Override
    public MapCodec<FunctionCallback> codec() {
        return CODEC;
    }
}

