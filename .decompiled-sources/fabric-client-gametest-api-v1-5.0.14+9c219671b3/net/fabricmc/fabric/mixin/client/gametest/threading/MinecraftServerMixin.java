/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.threading;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MinecraftServer.class})
public class MinecraftServerMixin {
    @WrapMethod(method={"runServer"})
    private void onRunServer(Operation<Void> original) {
        if (ThreadingImpl.isServerRunning) {
            throw new IllegalStateException("Server is already running");
        }
        ThreadingImpl.isServerRunning = true;
        ThreadingImpl.PHASER.register();
        try {
            original.call(new Object[0]);
        }
        finally {
            this.deregisterServer();
        }
    }

    @Inject(method={"runServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;onServerCrash(Lnet/minecraft/CrashReport;)V", shift=At.Shift.AFTER)})
    protected void onCrash(CallbackInfo ci) {
        if (ThreadingImpl.testFailureException == null) {
            ThreadingImpl.testFailureException = new Throwable("The server crashed");
        }
        Minecraft.getInstance().stop();
        ThreadingImpl.setGameCrashed();
        this.deregisterServer();
    }

    @Inject(method={"runServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;waitUntilNextTick()V")})
    private void preRunTasks(CallbackInfo ci) {
        if (!TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            ThreadingImpl.enterPhase(1);
        }
    }

    @Inject(method={"runServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;waitUntilNextTick()V", shift=At.Shift.AFTER)})
    private void postRunTasks(CallbackInfo ci) {
        NetworkSynchronizer.SERVERBOUND.waitForPacketHandlers((BlockableEventLoop)((Object)this));
        if (!TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            ThreadingImpl.enterPhase(2);
        }
        ThreadingImpl.serverCanAcceptTasks = true;
        ThreadingImpl.enterPhase(3);
        if (ThreadingImpl.testThread != null) {
            while (true) {
                try {
                    ThreadingImpl.SERVER_SEMAPHORE.acquire();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (ThreadingImpl.taskToRun == null) break;
                ThreadingImpl.taskToRun.run();
            }
        }
        ThreadingImpl.enterPhase(0);
    }

    @Inject(method={"shouldRun(Lnet/minecraft/server/TickTask;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void alwaysExecuteNetworkTask(CallbackInfoReturnable<Boolean> cir) {
        if (NetworkSynchronizer.SERVERBOUND.isRunningNetworkTasks()) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void deregisterServer() {
        ThreadingImpl.serverCanAcceptTasks = false;
        ThreadingImpl.PHASER.arriveAndDeregister();
        ThreadingImpl.isServerRunning = false;
        if (!ThreadingImpl.isGameCrashed()) {
            NetworkSynchronizer.SERVERBOUND.reset();
        }
    }
}

