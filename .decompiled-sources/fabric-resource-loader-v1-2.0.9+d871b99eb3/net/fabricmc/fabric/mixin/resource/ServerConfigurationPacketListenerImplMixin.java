/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import java.util.List;
import net.fabricmc.fabric.impl.resource.pack.FabricOriginalKnownPacksGetter;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ServerConfigurationPacketListenerImpl.class})
public abstract class ServerConfigurationPacketListenerImplMixin
extends ServerCommonPacketListenerImpl {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @ModifyArg(method={"startConfiguration"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/network/config/SynchronizeRegistriesTask;<init>(Ljava/util/List;Lnet/minecraft/core/LayeredRegistryAccess;)V", ordinal=0))
    public List<KnownPack> filterKnownPacks(List<KnownPack> currentKnownPacks) {
        return ((FabricOriginalKnownPacksGetter)((Object)this.server)).fabric$getOriginalKnownPacks().stream().filter(currentKnownPacks::contains).toList();
    }
}

