/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.impl.gamerule.EnumRuleCommand;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.gamerules.GameRule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.server.commands.GameRuleCommand$1"})
public abstract class GameRuleCommandVisitorMixin {
    @Final
    @Shadow
    LiteralArgumentBuilder<CommandSourceStack> val$base;

    @Inject(at={@At(value="HEAD")}, method={"visit"}, cancellable=true)
    private <T> void onRegisterCommand(GameRule<T> rule, CallbackInfo ci) {
        if (((RuleTypeExtensions)((Object)rule)).fabric_getType() == FabricGameRuleType.ENUM) {
            EnumRuleCommand.register(this.val$base, rule);
            ci.cancel();
        }
    }
}

