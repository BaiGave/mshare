/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestDedicatedServerContext
extends TestServerContext,
AutoCloseable {
    public TestServerConnection connect();

    @Override
    public void close();
}

