/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerMap {
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<ServerPlayer>();

    public Set<ServerPlayer> getAllPlayers() {
        return this.players.keySet();
    }

    public void addPlayer(ServerPlayer player, boolean ignored) {
        this.players.put(player, ignored);
    }

    public void removePlayer(ServerPlayer player) {
        this.players.removeBoolean(player);
    }

    public void ignorePlayer(ServerPlayer player) {
        this.players.replace(player, true);
    }

    public void unIgnorePlayer(ServerPlayer player) {
        this.players.replace(player, false);
    }

    public boolean ignoredOrUnknown(ServerPlayer player) {
        return this.players.getOrDefault((Object)player, true);
    }

    public boolean ignored(ServerPlayer player) {
        return this.players.getBoolean(player);
    }
}

