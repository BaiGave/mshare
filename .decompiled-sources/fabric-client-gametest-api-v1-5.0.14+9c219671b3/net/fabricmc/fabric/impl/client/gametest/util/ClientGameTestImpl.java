/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.util;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientGameTestImpl {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

    private ClientGameTestImpl() {
    }

    public static void waitForWorldLoad(ClientGameTestContext context) {
        for (int i = 0; i < 1200; ++i) {
            if (context.computeOnClient(client -> ClientGameTestImpl.isExperimentalWarningScreen(client.screen)).booleanValue()) {
                context.clickScreenButton("gui.yes");
            }
            if (context.computeOnClient(client -> client.screen instanceof BackupConfirmScreen).booleanValue()) {
                context.clickScreenButton("selectWorld.backupJoinSkipButton");
            }
            if (context.computeOnClient(ClientGameTestImpl::isWorldLoadingFinished).booleanValue()) {
                return;
            }
            context.waitTick();
        }
        if (!context.computeOnClient(ClientGameTestImpl::isWorldLoadingFinished).booleanValue()) {
            throw new AssertionError((Object)"Timeout loading world");
        }
    }

    private static boolean isExperimentalWarningScreen(Screen screen) {
        if (!(screen instanceof ConfirmScreen)) {
            return false;
        }
        ComponentContents componentContents = screen.getTitle().getContents();
        if (!(componentContents instanceof TranslatableContents)) {
            return false;
        }
        TranslatableContents translatableContents = (TranslatableContents)componentContents;
        return "selectWorld.warning.experimental.title".equals(translatableContents.getKey());
    }

    private static boolean isWorldLoadingFinished(Minecraft client) {
        LOGGER.info("World loading finished: {} screen: {}", (Object)client.level, (Object)client.screen);
        return client.level != null && !(client.screen instanceof LevelLoadingScreen);
    }
}

