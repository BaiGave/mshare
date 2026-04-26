/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.context;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableObject;

public class TestServerContextImpl
implements TestServerContext {
    protected final MinecraftServer server;

    public TestServerContextImpl(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void runCommand(String command) {
        ThreadingImpl.checkOnGametestThread("runCommand");
        Preconditions.checkNotNull(command, "command");
        this.runOnServer(server -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
    }

    @Override
    public <E extends Throwable> void runOnServer(FailableConsumer<MinecraftServer, E> action) throws E {
        ThreadingImpl.checkOnGametestThread("runOnServer");
        Preconditions.checkNotNull(action, "action");
        ThreadingImpl.runOnServer(() -> action.accept(this.server));
    }

    @Override
    public <T, E extends Throwable> T computeOnServer(FailableFunction<MinecraftServer, T, E> function) throws E {
        ThreadingImpl.checkOnGametestThread("computeOnServer");
        Preconditions.checkNotNull(function, "function");
        MutableObject result = new MutableObject();
        ThreadingImpl.runOnServer(() -> result.setValue(function.apply(this.server)));
        return result.getValue();
    }
}

