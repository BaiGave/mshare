/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.impl.client.gametest.context.TestClientLevelContextImpl;
import net.fabricmc.fabric.impl.client.gametest.context.TestServerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TestSingleplayerContextImpl
implements TestSingleplayerContext {
    private final ClientGameTestContext context;
    private final TestWorldSave worldSave;
    private final TestClientLevelContext clientLevel;
    private final TestServerContext server;

    public TestSingleplayerContextImpl(ClientGameTestContext context, TestWorldSave worldSave, MinecraftServer server) {
        this.context = context;
        this.worldSave = worldSave;
        this.clientLevel = new TestClientLevelContextImpl(context);
        this.server = new TestServerContextImpl(server);
    }

    @Override
    public TestWorldSave getWorldSave() {
        return this.worldSave;
    }

    @Override
    public TestClientLevelContext getClientLevel() {
        return this.clientLevel;
    }

    @Override
    public TestServerContext getServer() {
        return this.server;
    }

    @Override
    public void close() {
        ThreadingImpl.checkOnGametestThread("close");
        this.context.runOnClient(client -> {
            if (client.level == null) {
                throw new IllegalStateException("Exited the world before closing singleplayer context");
            }
            client.level.disconnect(Component.translatable("menu.savingLevel"));
            client.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")), false);
        });
        this.context.waitFor(client -> !ThreadingImpl.isServerRunning && client.level == null, 1200);
        this.context.waitTicks(2);
        this.context.setScreen(TitleScreen::new);
    }
}

