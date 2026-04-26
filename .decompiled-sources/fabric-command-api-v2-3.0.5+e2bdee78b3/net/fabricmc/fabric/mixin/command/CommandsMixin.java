/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Commands.class})
public abstract class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(at={@At(value="INVOKE", target="Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V")}, method={"<init>"})
    private void fabric_addCommands(Commands.CommandSelection selection, CommandBuildContext buildContext, CallbackInfo ci) {
        CommandRegistrationCallback.EVENT.invoker().register(this.dispatcher, buildContext, selection);
    }
}

