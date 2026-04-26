/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.DebugConfigCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Commands.class})
public class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/commands/BanIpCommands;register(Lcom/mojang/brigadier/CommandDispatcher;)V")})
    private void init(Commands.CommandSelection environment, CommandBuildContext context, CallbackInfo ci) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return;
        }
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        DebugConfigCommand.register(this.dispatcher, context);
    }
}

