/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.fabric.mixin.networking.accessor.ChunkMapAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.phys.Vec3;

public final class PlayerLookup {
    public static Collection<ServerPlayer> all(MinecraftServer server) {
        Objects.requireNonNull(server, "The server cannot be null");
        if (server.getPlayerList() != null) {
            return Collections.unmodifiableCollection(server.getPlayerList().getPlayers());
        }
        return Collections.emptyList();
    }

    public static Collection<ServerPlayer> level(ServerLevel level) {
        Objects.requireNonNull(level, "The level cannot be null");
        return Collections.unmodifiableCollection(level.players());
    }

    public static Collection<ServerPlayer> tracking(ServerLevel level, ChunkPos pos) {
        Objects.requireNonNull(level, "The level cannot be null");
        Objects.requireNonNull(pos, "The chunk pos cannot be null");
        return level.getChunkSource().chunkMap.getPlayers(pos, false);
    }

    public static Collection<ServerPlayer> tracking(Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        ChunkSource manager = entity.level().getChunkSource();
        if (manager instanceof ServerChunkCache) {
            ChunkMap chunkMap = ((ServerChunkCache)manager).chunkMap;
            EntityTrackerAccessor tracker = (EntityTrackerAccessor)((ChunkMapAccessor)((Object)chunkMap)).getEntityMap().get(entity.getId());
            if (tracker != null) {
                return tracker.getSeenBy().stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
            }
            return Collections.emptySet();
        }
        throw new IllegalArgumentException("Only supported on server levels!");
    }

    public static Collection<ServerPlayer> tracking(BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "BlockEntity cannot be null");
        if (!blockEntity.hasLevel() || blockEntity.getLevel().isClientSide()) {
            throw new IllegalArgumentException("Only supported on server levels!");
        }
        return PlayerLookup.tracking((ServerLevel)blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static Collection<ServerPlayer> tracking(ServerLevel level, BlockPos pos) {
        Objects.requireNonNull(pos, "BlockPos cannot be null");
        return PlayerLookup.tracking(level, ChunkPos.containing(pos));
    }

    public static Collection<ServerPlayer> around(ServerLevel level, Vec3 pos, double radius) {
        double radiusSq = radius * radius;
        return PlayerLookup.level(level).stream().filter(p -> p.distanceToSqr(pos) <= radiusSq).collect(Collectors.toList());
    }

    public static Collection<ServerPlayer> around(ServerLevel level, Vec3i pos, double radius) {
        double radiusSq = radius * radius;
        return PlayerLookup.level(level).stream().filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= radiusSq).collect(Collectors.toList());
    }

    private PlayerLookup() {
    }
}

