/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.jsonrpc.api.PlayerDto;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.Message;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class PlayerService {
    private static final Component DEFAULT_KICK_MESSAGE = Component.translatable("multiplayer.disconnect.kicked");

    public static List<PlayerDto> get(MinecraftApi minecraftApi) {
        return minecraftApi.playerListService().getPlayers().stream().map(PlayerDto::from).toList();
    }

    public static List<PlayerDto> kick(MinecraftApi minecraftApi, List<KickDto> kick, ClientInfo clientInfo) {
        ArrayList<PlayerDto> kicked = new ArrayList<PlayerDto>();
        for (KickDto kickDto : kick) {
            ServerPlayer serverPlayer = PlayerService.getServerPlayer(minecraftApi, kickDto.player());
            if (serverPlayer == null) continue;
            minecraftApi.playerListService().remove(serverPlayer, clientInfo);
            serverPlayer.connection.disconnect(kickDto.message.flatMap(Message::asComponent).orElse(DEFAULT_KICK_MESSAGE));
            kicked.add(kickDto.player());
        }
        return kicked;
    }

    private static @Nullable ServerPlayer getServerPlayer(MinecraftApi minecraftApi, PlayerDto playerDto) {
        if (playerDto.id().isPresent()) {
            return minecraftApi.playerListService().getPlayer(playerDto.id().get());
        }
        if (playerDto.name().isPresent()) {
            return minecraftApi.playerListService().getPlayerByName(playerDto.name().get());
        }
        return null;
    }

    public record KickDto(PlayerDto player, Optional<Message> message) {
        public static final MapCodec<KickDto> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)PlayerDto.CODEC.codec().fieldOf("player")).forGetter(KickDto::player), Message.CODEC.optionalFieldOf("message").forGetter(KickDto::message)).apply((Applicative<KickDto, ?>)i, KickDto::new));
    }
}

