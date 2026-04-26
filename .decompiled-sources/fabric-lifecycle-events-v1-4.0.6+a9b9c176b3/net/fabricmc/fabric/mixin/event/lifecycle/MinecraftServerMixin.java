/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MinecraftServer.class})
public abstract class MinecraftServerMixin {
    @Shadow
    private MinecraftServer.ReloadableResources resources;

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;initServer()Z")}, method={"runServer"})
    private void beforeSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting((MinecraftServer)((Object)this));
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal=0)}, method={"runServer"})
    private void afterSetupServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STARTED.invoker().onServerStarted((MinecraftServer)((Object)this));
    }

    @Inject(at={@At(value="HEAD")}, method={"stopServer"})
    private void beforeShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping((MinecraftServer)((Object)this));
    }

    @Inject(at={@At(value="TAIL")}, method={"stopServer"})
    private void afterShutdownServer(CallbackInfo info) {
        ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped((MinecraftServer)((Object)this));
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;tickChildren(Ljava/util/function/BooleanSupplier;)V")}, method={"tickServer"})
    private void onStartTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerTickEvents.START_SERVER_TICK.invoker().onStartTick((MinecraftServer)((Object)this));
    }

    @Inject(at={@At(value="TAIL")}, method={"tickServer"})
    private void onEndTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        ServerTickEvents.END_SERVER_TICK.invoker().onEndTick((MinecraftServer)((Object)this));
    }

    @WrapOperation(method={"createLevels"}, at={@At(value="INVOKE", target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")})
    private <K, V> V onLoadWorld(Map<K, V> levels, K dimension, V level, Operation<V> original) {
        V result = original.call(levels, dimension, level);
        ServerLevelEvents.LOAD.invoker().onLevelLoad((MinecraftServer)((Object)this), (ServerLevel)level);
        return result;
    }

    @Inject(method={"stopServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerLevel;close()V")})
    private void onUnloadWorldAtShutdown(CallbackInfo ci, @Local(name={"level"}) ServerLevel level) {
        ServerLevelEvents.UNLOAD.invoker().onLevelUnload((MinecraftServer)((Object)this), level);
    }

    @Inject(method={"reloadResources"}, at={@At(value="HEAD")})
    private void startResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.invoker().startDataPackReload((MinecraftServer)((Object)this), this.resources.resourceManager());
    }

    @Inject(method={"reloadResources"}, at={@At(value="TAIL")})
    private void endResourceReload(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handleAsync((value, throwable) -> {
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.invoker().endDataPackReload((MinecraftServer)((Object)this), this.resources.resourceManager(), throwable == null);
            return value;
        }, (Executor)((MinecraftServer)((Object)this)));
    }

    @Inject(method={"saveAllChunks"}, at={@At(value="HEAD")})
    private void startSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvents.BEFORE_SAVE.invoker().onBeforeSave((MinecraftServer)((Object)this), flush, force);
    }

    @Inject(method={"saveAllChunks"}, at={@At(value="TAIL")})
    private void endSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleEvents.AFTER_SAVE.invoker().onAfterSave((MinecraftServer)((Object)this), flush, force);
    }
}

