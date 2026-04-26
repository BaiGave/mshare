/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import net.fabricmc.fabric.impl.event.interaction.FakePlayerPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class FakePlayer
extends ServerPlayer {
    public static final UUID DEFAULT_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[Minecraft]");
    private static final Map<FakePlayerKey, FakePlayer> FAKE_PLAYER_MAP = new MapMaker().weakValues().makeMap();

    public static FakePlayer get(ServerLevel level) {
        return FakePlayer.get(level, DEFAULT_PROFILE);
    }

    public static FakePlayer get(ServerLevel level, GameProfile profile) {
        Objects.requireNonNull(level, "Level may not be null.");
        Objects.requireNonNull(profile, "Game profile may not be null.");
        return FAKE_PLAYER_MAP.computeIfAbsent(new FakePlayerKey(level, profile), key -> new FakePlayer(key.level, key.profile));
    }

    protected FakePlayer(ServerLevel level, GameProfile profile) {
        super(level.getServer(), level, profile, ClientInformation.createDefault());
        this.connection = new FakePlayerPacketListener(this);
    }

    @Override
    public void tick() {
    }

    @Override
    public void updateOptions(ClientInformation settings) {
    }

    @Override
    public void awardStat(Stat<?> stat, int amount) {
    }

    @Override
    public void resetStat(Stat<?> stat) {
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
        return true;
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        return null;
    }

    @Override
    public void startSleeping(BlockPos pos) {
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        return false;
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean front) {
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider factory) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container inventory) {
    }

    private record FakePlayerKey(ServerLevel level, GameProfile profile) {
    }
}

