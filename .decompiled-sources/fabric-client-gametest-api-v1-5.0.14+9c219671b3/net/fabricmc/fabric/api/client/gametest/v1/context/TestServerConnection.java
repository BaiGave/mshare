/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestServerConnection
extends AutoCloseable {
    public TestClientLevelContext getClientLevel();

    @Override
    public void close();
}

