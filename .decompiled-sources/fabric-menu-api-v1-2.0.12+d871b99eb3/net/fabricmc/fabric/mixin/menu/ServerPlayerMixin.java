/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.menu;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.impl.menu.Networking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerPlayer.class})
public abstract class ServerPlayerMixin
extends Player {
    @Shadow
    private int containerCounter;

    private ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    @Shadow
    public abstract void closeContainer();

    @Redirect(method={"openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
    private void fabric_closeContainerScreenIfAllowed(ServerPlayer player, MenuProvider factory) {
        if (factory.shouldCloseCurrentScreen()) {
            this.closeContainer();
        } else {
            this.doCloseContainer();
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    @Inject(method={"openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V")})
    private void fabric_storeOpenedMenu(MenuProvider factory, CallbackInfoReturnable<OptionalInt> info, @Local(name={"menu"}) AbstractContainerMenu menu) {
        block3: {
            block2: {
                if (factory instanceof ExtendedMenuProvider) break block2;
                if (!(factory instanceof SimpleMenuProvider)) break block3;
                SimpleMenuProvider simpleFactory = (SimpleMenuProvider)factory;
                if (!(simpleFactory.menuConstructor instanceof ExtendedMenuProvider)) break block3;
            }
            this.containerMenu = menu;
            return;
        }
        if (!(menu.getType() instanceof ExtendedMenuType)) return;
        Identifier id = BuiltInRegistries.MENU.getKey(menu.getType());
        throw new IllegalArgumentException("[Fabric] Extended menu " + String.valueOf(id) + " must be opened with an ExtendedMenuProvider!");
    }

    /*
     * Enabled aggressive block sorting
     */
    @Redirect(method={"openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void fabric_replaceVanillaScreenPacket(ServerGamePacketListenerImpl networkHandler, Packet<?> packet, MenuProvider factory) {
        if (factory instanceof SimpleMenuProvider) {
            SimpleMenuProvider simpleProvider = (SimpleMenuProvider)factory;
            MenuConstructor menuConstructor = simpleProvider.menuConstructor;
            if (menuConstructor instanceof ExtendedMenuProvider) {
                ExtendedMenuProvider extendedProvider = (ExtendedMenuProvider)menuConstructor;
                factory = extendedProvider;
            }
        }
        if (!(factory instanceof ExtendedMenuProvider)) {
            networkHandler.send(packet);
            return;
        }
        ExtendedMenuProvider extendedFactory = (ExtendedMenuProvider)factory;
        AbstractContainerMenu handler = Objects.requireNonNull(this.containerMenu);
        if (handler.getType() instanceof ExtendedMenuType) {
            Networking.sendOpenPacket((ServerPlayer)((Object)this), extendedFactory, handler, this.containerCounter);
            return;
        }
        Identifier id = BuiltInRegistries.MENU.getKey(handler.getType());
        throw new IllegalArgumentException("[Fabric] Non-extended menu " + String.valueOf(id) + " must not be opened with an ExtendedMenuProvider!");
    }
}

