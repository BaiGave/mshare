/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.world;

import com.google.common.base.Preconditions;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.impl.client.gametest.context.TestSingleplayerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public final class TestWorldSaveImpl
implements TestWorldSave {
    private final ClientGameTestContext context;
    private final Path saveDirectory;

    public TestWorldSaveImpl(ClientGameTestContext context, Path saveDirectory) {
        this.context = context;
        this.saveDirectory = saveDirectory;
    }

    @Override
    public Path getSaveDirectory() {
        return this.saveDirectory;
    }

    @Override
    public TestSingleplayerContext open() {
        ThreadingImpl.checkOnGametestThread("open");
        Preconditions.checkState(!ThreadingImpl.isServerRunning, "Cannot open a world when a server is running");
        this.context.runOnClient(client -> client.createWorldOpenFlows().openWorld(this.saveDirectory.getFileName().toString(), () -> {
            throw new AssertionError((Object)"World opening should not be canceled");
        }));
        ClientGameTestImpl.waitForWorldLoad(this.context);
        MinecraftServer server = this.context.computeOnClient(Minecraft::getSingleplayerServer);
        return new TestSingleplayerContextImpl(this.context, this, server);
    }
}

