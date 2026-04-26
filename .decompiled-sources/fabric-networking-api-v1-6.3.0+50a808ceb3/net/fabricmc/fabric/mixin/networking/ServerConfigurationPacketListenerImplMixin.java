/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.impl.networking.FabricRegistryFriendlyByteBuf;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerConfigurationPacketListenerImpl.class}, priority=900)
public abstract class ServerConfigurationPacketListenerImplMixin
extends ServerCommonPacketListenerImpl
implements PacketListenerExtensions,
FabricServerConfigurationPacketListenerImpl {
    @Shadow
    private @Nullable ConfigurationTask currentTask;
    @Shadow
    @Final
    private Queue<ConfigurationTask> configurationTasks;
    @Unique
    private ServerConfigurationNetworkAddon addon;
    @Unique
    private boolean sentConfiguration;
    @Unique
    private boolean earlyTaskExecution;

    @Shadow
    protected abstract void finishCurrentTask(ConfigurationTask.Type var1);

    @Override
    @Shadow
    public abstract boolean isAcceptingMessages();

    @Shadow
    public abstract void startConfiguration();

    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie arg) {
        super(server, connection, arg);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(CallbackInfo ci) {
        this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationPacketListenerImpl)((Object)this), this.server);
        this.addon.lateInit();
    }

    @Inject(method={"startConfiguration"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClientReady(CallbackInfo ci) {
        if (this.addon.startConfiguration()) {
            if (this.currentTask != null) {
                throw new IllegalStateException("A task is already running: " + this.currentTask.type().id());
            }
            ci.cancel();
            return;
        }
        if (!this.sentConfiguration) {
            this.addon.preConfiguration();
            this.sentConfiguration = true;
            this.earlyTaskExecution = true;
        }
        if (this.earlyTaskExecution) {
            if (this.pollEarlyTasks()) {
                ci.cancel();
                return;
            }
            this.earlyTaskExecution = false;
        }
        if (this.currentTask != null || !this.configurationTasks.isEmpty()) {
            throw new IllegalStateException("All early tasks should have been completed, current: " + String.valueOf(this.currentTask) + ", queued: " + this.configurationTasks.size());
        }
        this.addon.configuration();
    }

    @Unique
    private boolean pollEarlyTasks() {
        if (!this.earlyTaskExecution) {
            throw new IllegalStateException("Early task execution has finished");
        }
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
        }
        if (!this.isAcceptingMessages()) {
            return false;
        }
        ConfigurationTask task = this.configurationTasks.poll();
        if (task != null) {
            this.currentTask = task;
            task.start(this::send);
            return true;
        }
        return false;
    }

    public ServerConfigurationNetworkAddon getAddon() {
        return this.addon;
    }

    @Override
    public void addTask(ConfigurationTask task) {
        this.configurationTasks.add(task);
    }

    @Override
    public void completeTask(ConfigurationTask.Type key) {
        ConfigurationTask.Type currentKey;
        if (!this.earlyTaskExecution) {
            this.finishCurrentTask(key);
            return;
        }
        ConfigurationTask.Type type = currentKey = this.currentTask != null ? this.currentTask.type() : null;
        if (!key.equals(currentKey)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + String.valueOf(currentKey) + ", requested: " + String.valueOf(key));
        }
        this.currentTask = null;
        this.startConfiguration();
    }

    @WrapOperation(method={"handleConfigurationFinished"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;)Ljava/util/function/Function;")})
    private Function<ByteBuf, RegistryFriendlyByteBuf> bindChannelInfo(RegistryAccess registryManager, Operation<Function<ByteBuf, RegistryFriendlyByteBuf>> original) {
        return original.call(registryManager).andThen(registryByteBuf -> {
            FabricRegistryFriendlyByteBuf fabricRegistryFriendlyByteBuf = (FabricRegistryFriendlyByteBuf)((Object)registryByteBuf);
            fabricRegistryFriendlyByteBuf.fabric_setSendableConfigurationChannels(Set.copyOf(this.addon.getSendableChannels()));
            return registryByteBuf;
        });
    }
}

