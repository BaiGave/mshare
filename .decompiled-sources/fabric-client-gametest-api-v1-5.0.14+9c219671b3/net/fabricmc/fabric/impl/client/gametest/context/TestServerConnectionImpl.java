/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class TestServerConnectionImpl
implements TestServerConnection {
    private final ClientGameTestContext context;
    private final TestClientLevelContext clientLevel;

    public TestServerConnectionImpl(ClientGameTestContext context, TestClientLevelContext clientLevel) {
        this.context = context;
        this.clientLevel = clientLevel;
    }

    @Override
    public TestClientLevelContext getClientLevel() {
        return this.clientLevel;
    }

    @Override
    public void close() {
        ThreadingImpl.checkOnGametestThread("close");
        this.context.runOnClient(client -> {
            if (client.level == null) {
                throw new AssertionError((Object)"Disconnected from server before closing the test server connection");
            }
            client.level.disconnect(Component.literal("Disconnecting"));
            client.disconnectWithSavingScreen();
        });
        this.context.waitFor(client -> client.level == null);
        this.context.waitTicks(2);
        this.context.setScreen(TitleScreen::new);
    }
}

