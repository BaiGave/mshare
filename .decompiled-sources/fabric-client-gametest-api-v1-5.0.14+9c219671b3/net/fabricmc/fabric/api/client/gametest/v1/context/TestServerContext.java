/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.gametest.v1.context;

import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface TestServerContext {
    public void runCommand(String var1);

    public <E extends Throwable> void runOnServer(FailableConsumer<MinecraftServer, E> var1) throws E;

    public <T, E extends Throwable> T computeOnServer(FailableFunction<MinecraftServer, T, E> var1) throws E;
}

