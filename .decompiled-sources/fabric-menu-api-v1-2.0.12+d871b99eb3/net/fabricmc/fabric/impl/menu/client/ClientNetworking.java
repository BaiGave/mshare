/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.menu.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.impl.menu.Networking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientNetworking
implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-menu-api-v1/client");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Networking.OpenScreenPayload.ID, (payload, context) -> this.openScreen((Networking.OpenScreenPayload)payload));
    }

    private <D> void openScreen(Networking.OpenScreenPayload<D> payload) {
        Identifier typeId = payload.identifier();
        int syncId = payload.containerId();
        Component title = payload.title();
        MenuType<?> type = BuiltInRegistries.MENU.getValue(typeId);
        if (type == null || payload.data() == null) {
            LOGGER.warn("Unknown menu ID: {}", (Object)typeId);
            return;
        }
        if (!(type instanceof ExtendedMenuType)) {
            LOGGER.warn("Received extended opening packet for non-extended menu {}", (Object)typeId);
            return;
        }
        MenuScreens.ScreenConstructor<?, ?> screenFactory = MenuScreens.getConstructor(type);
        if (screenFactory != null) {
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            Object screen = screenFactory.create(((ExtendedMenuType)type).create(syncId, player.getInventory(), payload.data()), player.getInventory(), title);
            player.containerMenu = ((MenuAccess)screen).getMenu();
            client.setScreen((Screen)screen);
        } else {
            LOGGER.warn("Screen not registered for menu {}!", (Object)typeId);
        }
    }
}

