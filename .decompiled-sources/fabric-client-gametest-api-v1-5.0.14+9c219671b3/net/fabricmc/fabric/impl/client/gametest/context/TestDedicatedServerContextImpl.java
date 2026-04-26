/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.impl.client.gametest.context.TestClientLevelContextImpl;
import net.fabricmc.fabric.impl.client.gametest.context.TestServerConnectionImpl;
import net.fabricmc.fabric.impl.client.gametest.context.TestServerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.dedicated.DedicatedServer;

public class TestDedicatedServerContextImpl
extends TestServerContextImpl
implements TestDedicatedServerContext {
    private final ClientGameTestContext context;

    public TestDedicatedServerContextImpl(ClientGameTestContext context, DedicatedServer server) {
        super(server);
        this.context = context;
    }

    @Override
    public TestServerConnection connect() {
        ThreadingImpl.checkOnGametestThread("connect");
        this.context.runOnClient(client -> {
            ServerData serverInfo = new ServerData("localhost", this.getConnectionAddress(), ServerData.Type.OTHER);
            ConnectScreen.startConnecting(client.screen, client, ServerAddress.parseString(this.getConnectionAddress()), serverInfo, false, null);
        });
        ClientGameTestImpl.waitForWorldLoad(this.context);
        TestClientLevelContextImpl clientLevel = new TestClientLevelContextImpl(this.context);
        return new TestServerConnectionImpl(this.context, clientLevel);
    }

    private String getConnectionAddress() {
        return "localhost:" + this.server.getPort();
    }

    @Override
    public void close() {
        ThreadingImpl.checkOnGametestThread("close");
        if (!ThreadingImpl.isServerRunning || !this.server.getRunningThread().isAlive()) {
            throw new AssertionError((Object)"Stopped the dedicated server before closing the dedicated server context");
        }
        this.server.halt(false);
        this.context.waitFor(client -> !ThreadingImpl.isServerRunning && !this.server.getRunningThread().isAlive());
    }
}

