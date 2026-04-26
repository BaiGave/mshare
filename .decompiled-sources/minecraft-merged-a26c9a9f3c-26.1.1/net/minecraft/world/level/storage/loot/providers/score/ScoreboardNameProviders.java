/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.FixedScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;

public class ScoreboardNameProviders {
    private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE.byNameCodec().dispatch(ScoreboardNameProvider::codec, c -> c);
    public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(() -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC).xmap(Either::unwrap, provider -> {
        Either<Object, ScoreboardNameProvider> either;
        if (provider instanceof ContextScoreboardNameProvider) {
            ContextScoreboardNameProvider context = (ContextScoreboardNameProvider)provider;
            either = Either.left(context);
        } else {
            either = Either.right(provider);
        }
        return either;
    }));

    public static MapCodec<? extends ScoreboardNameProvider> bootstrap(Registry<MapCodec<? extends ScoreboardNameProvider>> registry) {
        Registry.register(registry, "fixed", FixedScoreboardNameProvider.MAP_CODEC);
        return Registry.register(registry, "context", ContextScoreboardNameProvider.MAP_CODEC);
    }
}

