/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;

public final class ServerLifecycleEvents {
    public static final Event<ServerStarting> SERVER_STARTING = EventFactory.createArrayBacked(ServerStarting.class, callbacks -> server -> {
        for (ServerStarting callback : callbacks) {
            callback.onServerStarting(server);
        }
    });
    public static final Event<ServerStarted> SERVER_STARTED = EventFactory.createArrayBacked(ServerStarted.class, callbacks -> server -> {
        for (ServerStarted callback : callbacks) {
            callback.onServerStarted(server);
        }
    });
    public static final Event<ServerStopping> SERVER_STOPPING = EventFactory.createArrayBacked(ServerStopping.class, callbacks -> server -> {
        for (ServerStopping callback : callbacks) {
            callback.onServerStopping(server);
        }
    });
    public static final Event<ServerStopped> SERVER_STOPPED = EventFactory.createArrayBacked(ServerStopped.class, callbacks -> server -> {
        for (ServerStopped callback : callbacks) {
            callback.onServerStopped(server);
        }
    });
    public static final Event<SyncDataPackContents> SYNC_DATA_PACK_CONTENTS = EventFactory.createArrayBacked(SyncDataPackContents.class, callbacks -> (player, joined) -> {
        for (SyncDataPackContents callback : callbacks) {
            callback.onSyncDataPackContents(player, joined);
        }
    });
    public static final Event<StartDataPackReload> START_DATA_PACK_RELOAD = EventFactory.createArrayBacked(StartDataPackReload.class, callbacks -> (server, resourceManager) -> {
        for (StartDataPackReload callback : callbacks) {
            callback.startDataPackReload(server, resourceManager);
        }
    });
    public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = EventFactory.createArrayBacked(EndDataPackReload.class, callbacks -> (server, resourceManager, success) -> {
        for (EndDataPackReload callback : callbacks) {
            callback.endDataPackReload(server, resourceManager, success);
        }
    });
    public static final Event<BeforeSave> BEFORE_SAVE = EventFactory.createArrayBacked(BeforeSave.class, callbacks -> (server, flush, force) -> {
        for (BeforeSave callback : callbacks) {
            callback.onBeforeSave(server, flush, force);
        }
    });
    public static final Event<AfterSave> AFTER_SAVE = EventFactory.createArrayBacked(AfterSave.class, callbacks -> (server, flush, force) -> {
        for (AfterSave callback : callbacks) {
            callback.onAfterSave(server, flush, force);
        }
    });

    private ServerLifecycleEvents() {
    }

    @FunctionalInterface
    public static interface AfterSave {
        public void onAfterSave(MinecraftServer var1, boolean var2, boolean var3);
    }

    @FunctionalInterface
    public static interface BeforeSave {
        public void onBeforeSave(MinecraftServer var1, boolean var2, boolean var3);
    }

    @FunctionalInterface
    public static interface EndDataPackReload {
        public void endDataPackReload(MinecraftServer var1, CloseableResourceManager var2, boolean var3);
    }

    @FunctionalInterface
    public static interface StartDataPackReload {
        public void startDataPackReload(MinecraftServer var1, CloseableResourceManager var2);
    }

    @FunctionalInterface
    public static interface SyncDataPackContents {
        public void onSyncDataPackContents(ServerPlayer var1, boolean var2);
    }

    @FunctionalInterface
    public static interface ServerStopped {
        public void onServerStopped(MinecraftServer var1);
    }

    @FunctionalInterface
    public static interface ServerStopping {
        public void onServerStopping(MinecraftServer var1);
    }

    @FunctionalInterface
    public static interface ServerStarted {
        public void onServerStarted(MinecraftServer var1);
    }

    @FunctionalInterface
    public static interface ServerStarting {
        public void onServerStarting(MinecraftServer var1);
    }
}

