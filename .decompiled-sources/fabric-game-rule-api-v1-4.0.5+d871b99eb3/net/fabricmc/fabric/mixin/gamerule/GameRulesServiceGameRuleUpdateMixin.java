/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.server.jsonrpc.methods.InvalidParameterJsonRpcException;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRulesService.GameRuleUpdate.class})
public abstract class GameRulesServiceGameRuleUpdateMixin
implements FabricTypedRule {
    @Unique
    private @Nullable FabricGameRuleType fabricGameRuleType = null;

    @Override
    public @Nullable FabricGameRuleType getFabricType() {
        return this.fabricGameRuleType;
    }

    @Override
    public void setFabricType(FabricGameRuleType type) {
        this.fabricGameRuleType = Objects.requireNonNull(type);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private <T> void updateFabricType(GameRule<T> rule, Object value, CallbackInfo ci) {
        FabricGameRuleType type = ((RuleTypeExtensions)((Object)rule)).fabric_getType();
        if (type == null) {
            return;
        }
        this.setFabricType(type);
    }

    @ModifyReturnValue(method={"getValueAndTypeCodec"}, at={@At(value="RETURN")})
    private static <T, R extends GameRulesService.GameRuleUpdate<T>> MapCodec<R> getValueAndFabricTypeCodec(MapCodec<? extends GameRulesService.GameRuleUpdate<T>> original, GameRule<T> gameRule) {
        MapCodec<GameRulesService.GameRuleUpdate<T>> fabricTypedCodec = GameRulesServiceGameRuleUpdateMixin.fabric_createTypedCodec(gameRule);
        return Codec.mapEither(fabricTypedCodec, original).xmap(either -> (GameRulesService.GameRuleUpdate)either.map(Function.identity(), Function.identity()), typedRule -> ((FabricTypedRule)((Object)typedRule)).getFabricType() == null ? Either.right(typedRule) : Either.left(typedRule));
    }

    @Unique
    private static <T> GameRulesService.GameRuleUpdate<T> fabric_checkType(GameRule<T> gameRule, FabricGameRuleType type, T object) {
        FabricGameRuleType gameRuleType = ((RuleTypeExtensions)((Object)gameRule)).fabric_getType();
        if (gameRuleType != type) {
            throw new InvalidParameterJsonRpcException("Stated type \"" + String.valueOf(type) + "\" mismatches with actual type \"" + String.valueOf(gameRuleType) + "\" of gamerule \"" + gameRule.id() + "\"");
        }
        return new GameRulesService.GameRuleUpdate<T>(gameRule, object);
    }

    @Unique
    private static <T> MapCodec<? extends GameRulesService.GameRuleUpdate<T>> fabric_createTypedCodec(GameRule<T> rule) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)StringRepresentable.fromEnum(FabricGameRuleType::values).fieldOf("type")).forGetter(arg -> ((RuleTypeExtensions)((Object)arg.gameRule())).fabric_getType()), ((MapCodec)rule.valueCodec().fieldOf("value")).forGetter(GameRulesService.GameRuleUpdate::value)).apply((Applicative<GameRulesService.GameRuleUpdate, ?>)instance, (type, object) -> GameRulesServiceGameRuleUpdateMixin.fabric_checkType(rule, type, object)));
    }
}

