/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.timers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;

public record FunctionTagCallback(Identifier tagId) implements TimerCallback<MinecraftServer>
{
    public static final MapCodec<FunctionTagCallback> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(FunctionTagCallback::tagId)).apply((Applicative<FunctionTagCallback, ?>)i, FunctionTagCallback::new));

    @Override
    public void handle(MinecraftServer server, TimerQueue<MinecraftServer> queue, long time) {
        ServerFunctionManager functionManager = server.getFunctions();
        List<CommandFunction<CommandSourceStack>> tag = functionManager.getTag(this.tagId);
        for (CommandFunction<CommandSourceStack> function : tag) {
            functionManager.execute(function, functionManager.getGameLoopSender());
        }
    }

    @Override
    public MapCodec<FunctionTagCallback> codec() {
        return CODEC;
    }
}

