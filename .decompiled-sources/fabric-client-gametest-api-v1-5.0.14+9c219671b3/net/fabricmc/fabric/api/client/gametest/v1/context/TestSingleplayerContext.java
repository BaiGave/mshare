/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestSingleplayerContext
extends AutoCloseable {
    public TestWorldSave getWorldSave();

    public TestClientLevelContext getClientLevel();

    public TestServerContext getServer();

    @Override
    public void close();
}

