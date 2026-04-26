/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.jsonrpc.methods;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;

public class GameRulesService {
    public static List<GameRuleUpdate<?>> get(MinecraftApi minecraftApi) {
        ArrayList rules = new ArrayList();
        minecraftApi.gameRuleService().getAvailableGameRules().forEach(gameRule -> GameRulesService.addGameRule(minecraftApi, gameRule, rules));
        return rules;
    }

    private static <T> void addGameRule(MinecraftApi minecraftApi, GameRule<T> gameRule, List<GameRuleUpdate<?>> rules) {
        T value = minecraftApi.gameRuleService().getRuleValue(gameRule);
        rules.add(GameRulesService.getTypedRule(minecraftApi, gameRule, value));
    }

    public static <T> GameRuleUpdate<T> getTypedRule(MinecraftApi minecraftApi, GameRule<T> gameRule, T value) {
        return minecraftApi.gameRuleService().getTypedRule(gameRule, value);
    }

    public static <T> GameRuleUpdate<T> update(MinecraftApi minecraftApi, GameRuleUpdate<T> update, ClientInfo clientInfo) {
        return minecraftApi.gameRuleService().updateGameRule(update, clientInfo);
    }

    public record GameRuleUpdate<T>(GameRule<T> gameRule, T value) {
        public static final Codec<GameRuleUpdate<?>> TYPED_CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueAndTypeCodec);
        public static final Codec<GameRuleUpdate<?>> CODEC = BuiltInRegistries.GAME_RULE.byNameCodec().dispatch("key", GameRuleUpdate::gameRule, GameRuleUpdate::getValueCodec);

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueCodec(GameRule<T> gameRule) {
            return ((MapCodec)gameRule.valueCodec().fieldOf("value")).xmap(value -> new GameRuleUpdate<Object>(gameRule, value), GameRuleUpdate::value);
        }

        private static <T> MapCodec<? extends GameRuleUpdate<T>> getValueAndTypeCodec(GameRule<T> gameRule) {
            return RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)StringRepresentable.fromEnum(GameRuleType::values).fieldOf("type")).forGetter(r -> r.gameRule.gameRuleType()), ((MapCodec)gameRule.valueCodec().fieldOf("value")).forGetter(GameRuleUpdate::value)).apply((Applicative<GameRuleUpdate, ?>)i, (type, value) -> GameRuleUpdate.getUntypedRule(gameRule, type, value)));
        }

        private static <T> GameRuleUpdate<T> getUntypedRule(GameRule<T> gameRule, GameRuleType readType, T value) {
            if (gameRule.gameRuleType() != readType) {
                throw new InvalidParameterJsonRpcException("Stated type \"" + String.valueOf(readType) + "\" mismatches with actual type \"" + String.valueOf(gameRule.gameRuleType()) + "\" of gamerule \"" + gameRule.id() + "\"");
            }
            return new GameRuleUpdate<T>(gameRule, value);
        }
    }
}

