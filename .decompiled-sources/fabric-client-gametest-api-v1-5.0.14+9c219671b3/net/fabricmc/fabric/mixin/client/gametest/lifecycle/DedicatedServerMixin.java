/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.lifecycle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DedicatedServer.class})
public abstract class DedicatedServerMixin {
    @Inject(method={"initServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/dedicated/DedicatedServer;loadLevel()V")})
    private void captureServerInstance(CallbackInfoReturnable<Boolean> cir) {
        CompletableFuture<DedicatedServer> serverFuture = DedicatedServerImplUtil.serverFuture;
        if (serverFuture != null) {
            serverFuture.complete((DedicatedServer)((Object)this));
        }
    }

    @WrapOperation(method={"stopServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Util;shutdownExecutors()V")})
    private void dontStopExecutors(Operation<Void> original) {
    }
}

