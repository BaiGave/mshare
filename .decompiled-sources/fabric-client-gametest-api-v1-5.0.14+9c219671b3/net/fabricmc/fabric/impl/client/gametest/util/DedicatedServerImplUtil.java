/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.minecraft.server.Main;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DedicatedServerImplUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
    private static final Properties DEFAULT_SERVER_PROPERTIES = Util.make(new Properties(), properties -> {
        properties.setProperty("online-mode", "false");
        properties.setProperty("sync-chunk-writes", String.valueOf(Util.getPlatform() == Util.OS.WINDOWS));
        properties.setProperty("spawn-protection", "0");
        properties.setProperty("max-players", "1");
    });
    public static @Nullable Path saveLevelDataTo = null;
    public static @Nullable CompletableFuture<DedicatedServer> serverFuture = null;

    private DedicatedServerImplUtil() {
    }

    public static DedicatedServer start(ClientGameTestContext context, Properties serverProperties) {
        DedicatedServer server;
        DedicatedServerImplUtil.setupServer(serverProperties);
        serverFuture = new CompletableFuture();
        new Thread(() -> Main.main(new String[0])).start();
        try {
            server = serverFuture.get(10L, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        finally {
            serverFuture = null;
        }
        context.waitFor(client -> ThreadingImpl.isServerRunning && ThreadingImpl.serverCanAcceptTasks);
        return server;
    }

    private static void setupServer(Properties customServerProperties) {
        Properties serverProperties = new Properties();
        serverProperties.putAll((Map<?, ?>)DEFAULT_SERVER_PROPERTIES);
        serverProperties.putAll((Map<?, ?>)customServerProperties);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("server.properties", new String[0]), new OpenOption[0]);){
            serverProperties.store(writer, null);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write server properties", e);
        }
    }
}

