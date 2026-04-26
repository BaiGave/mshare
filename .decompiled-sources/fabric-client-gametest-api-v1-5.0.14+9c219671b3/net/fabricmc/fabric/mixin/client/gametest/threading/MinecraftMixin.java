/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.threading;

import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.Optional;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Minecraft.class})
public class MinecraftMixin {
    @Unique
    private boolean inMergedRunTasksLoop = false;
    @Unique
    private Runnable deferredTask = null;

    @WrapMethod(method={"run"})
    private void onRun(Operation<Void> original) throws Throwable {
        if (ThreadingImpl.isClientRunning) {
            throw new IllegalStateException("Client is already running");
        }
        ThreadingImpl.isClientRunning = true;
        ThreadingImpl.PHASER.register();
        try {
            original.call(new Object[0]);
        }
        finally {
            MinecraftMixin.deregisterClient();
            if (ThreadingImpl.testFailureException != null) {
                throw ThreadingImpl.testFailureException;
            }
        }
    }

    @Inject(method={"emergencySave"}, at={@At(value="HEAD")})
    private void deregisterAfterCrash(CallbackInfo ci) {
        ThreadingImpl.setGameCrashed();
        MinecraftMixin.deregisterClient();
    }

    @ModifyExpressionValue(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/DeltaTracker$Timer;advanceGameTime(J)I")})
    private int captureTicksPerFrame(int capturedTicksPerFrame, @Share(value="ticksPerFrame") LocalIntRef ticksPerFrame) {
        if (capturedTicksPerFrame > 1) {
            capturedTicksPerFrame = 1;
        }
        ticksPerFrame.set(capturedTicksPerFrame);
        return capturedTicksPerFrame;
    }

    @Inject(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;runAllTasks()V")})
    private void preRunTasksHook(CallbackInfo ci) {
        if (!this.inMergedRunTasksLoop) {
            this.inMergedRunTasksLoop = true;
            this.preRunTasks();
        }
    }

    @Inject(method={"runTick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;runAllTasks()V", shift=At.Shift.AFTER)})
    private void postRunTasksHook(CallbackInfo ci, @Share(value="ticksPerFrame") LocalIntRef ticksPerFrame) {
        if (ticksPerFrame.get() > 0) {
            NetworkSynchronizer.CLIENTBOUND.waitForPacketHandlers((BlockableEventLoop)((Object)this));
            this.postRunTasks();
            this.inMergedRunTasksLoop = false;
        }
    }

    @Inject(method={"doWorldLoad"}, at={@At(value="HEAD")}, cancellable=true)
    private void deferStartIntegratedServer(LevelStorageSource.LevelStorageAccess storageAccess, PackRepository dataPackManager, WorldStem worldStem, Optional<GameRules> gameRules, boolean newWorld, CallbackInfo ci) {
        if (ThreadingImpl.taskToRun != null) {
            this.deferredTask = () -> Minecraft.getInstance().doWorldLoad(storageAccess, dataPackManager, worldStem, gameRules, newWorld);
            ci.cancel();
        }
    }

    @Inject(method={"doWorldLoad"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;managedBlock(Ljava/util/function/BooleanSupplier;)V")})
    private void onStartIntegratedServerBusyWait(CallbackInfo ci) {
        this.preRunTasks();
        this.postRunTasks();
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void deferDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        if (Minecraft.getInstance().getSingleplayerServer() != null && ThreadingImpl.taskToRun != null) {
            this.deferredTask = () -> Minecraft.getInstance().disconnect(disconnectionScreen, transferring);
            ci.cancel();
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;dropAllTasks()V")})
    private void onDisconnectCancelTasks(CallbackInfo ci) {
        NetworkSynchronizer.CLIENTBOUND.reset();
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;renderFrame(Z)V", shift=At.Shift.AFTER)})
    private void onDisconnectBusyWait(CallbackInfo ci) {
        this.preRunTasks();
        this.postRunTasks();
    }

    @Unique
    private void preRunTasks() {
        if (ThreadingImpl.getCurrentPhase() == 2) {
            this.postRunTasks();
        }
        if (!TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            ThreadingImpl.enterPhase(1);
            ThreadingImpl.enterPhase(2);
        }
    }

    @Unique
    private void postRunTasks() {
        ThreadingImpl.clientCanAcceptTasks = true;
        ThreadingImpl.enterPhase(3);
        if (ThreadingImpl.testThread != null) {
            while (true) {
                try {
                    ThreadingImpl.CLIENT_SEMAPHORE.acquire();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (ThreadingImpl.taskToRun == null) break;
                ThreadingImpl.taskToRun.run();
            }
        }
        ThreadingImpl.enterPhase(0);
        Runnable deferredTask = this.deferredTask;
        this.deferredTask = null;
        if (deferredTask != null) {
            deferredTask.run();
        }
    }

    @Inject(method={"getInstance"}, at={@At(value="HEAD")})
    private static void checkThreadOnGetInstance(CallbackInfoReturnable<Minecraft> cir) {
        Preconditions.checkState(Thread.currentThread() != ThreadingImpl.testThread, "Minecraft.getInstance() cannot be called from the gametest thread. Try using ClientGameTestContext.runOnClient or ClientGameTestContext.computeOnClient");
    }

    @Unique
    private static void deregisterClient() {
        if (ThreadingImpl.isClientRunning) {
            ThreadingImpl.clientCanAcceptTasks = false;
            ThreadingImpl.PHASER.arriveAndDeregister();
            ThreadingImpl.isClientRunning = false;
        }
    }
}

