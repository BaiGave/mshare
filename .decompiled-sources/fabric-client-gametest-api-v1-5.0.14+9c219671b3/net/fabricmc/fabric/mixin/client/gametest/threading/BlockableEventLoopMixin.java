/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.threading;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockableEventLoop.class})
public class BlockableEventLoopMixin {
    @Inject(method={"schedule"}, at={@At(value="HEAD")})
    private void onPacketHandlerSchedule(Runnable task, CallbackInfo ci) {
        BlockableEventLoopMixin blockableEventLoopMixin = this;
        Objects.requireNonNull(blockableEventLoopMixin);
        BlockableEventLoopMixin blockableEventLoopMixin2 = blockableEventLoopMixin;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Minecraft.class, MinecraftServer.class}, (Object)blockableEventLoopMixin2, n)) {
            case 0: {
                Minecraft $ = (Minecraft)((Object)blockableEventLoopMixin2);
                NetworkSynchronizer.CLIENTBOUND.preTaskAdded(task);
                break;
            }
            case 1: {
                MinecraftServer $ = (MinecraftServer)((Object)blockableEventLoopMixin2);
                NetworkSynchronizer.SERVERBOUND.preTaskAdded(task);
                break;
            }
        }
    }

    @Inject(method={"doRunTask"}, at={@At(value="INVOKE", target="Ljava/lang/Runnable;run()V", shift=At.Shift.AFTER)})
    private void onPacketHandlerRun(Runnable task, CallbackInfo ci) {
        BlockableEventLoopMixin blockableEventLoopMixin = this;
        Objects.requireNonNull(blockableEventLoopMixin);
        BlockableEventLoopMixin blockableEventLoopMixin2 = blockableEventLoopMixin;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Minecraft.class, MinecraftServer.class}, (Object)blockableEventLoopMixin2, n)) {
            case 0: {
                Minecraft $ = (Minecraft)((Object)blockableEventLoopMixin2);
                NetworkSynchronizer.CLIENTBOUND.postTaskRun(task);
                break;
            }
            case 1: {
                MinecraftServer $ = (MinecraftServer)((Object)blockableEventLoopMixin2);
                NetworkSynchronizer.SERVERBOUND.postTaskRun(task);
                break;
            }
        }
    }
}

