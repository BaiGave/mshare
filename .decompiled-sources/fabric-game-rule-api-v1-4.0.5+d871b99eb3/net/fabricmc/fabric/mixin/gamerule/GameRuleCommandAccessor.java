/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.gamerules.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={GameRuleCommand.class})
public interface GameRuleCommandAccessor {
    @Invoker(value="queryRule")
    public static <T> int callQueryRule(CommandSourceStack source, GameRule<T> gameRule) {
        throw new AssertionError((Object)"This shouldn't happen!");
    }
}

