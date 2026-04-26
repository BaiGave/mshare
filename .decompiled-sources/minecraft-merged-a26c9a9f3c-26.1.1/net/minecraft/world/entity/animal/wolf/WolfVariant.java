/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.wolf;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record WolfVariant(AssetInfo adultInfo, AssetInfo babyInfo, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition>
{
    public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)AssetInfo.CODEC.fieldOf("assets")).forGetter(WolfVariant::adultInfo), ((MapCodec)AssetInfo.CODEC.fieldOf("baby_assets")).forGetter(WolfVariant::babyInfo), ((MapCodec)SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions")).forGetter(WolfVariant::spawnConditions)).apply((Applicative<WolfVariant, ?>)i, WolfVariant::new));
    public static final Codec<WolfVariant> NETWORK_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)AssetInfo.CODEC.fieldOf("assets")).forGetter(WolfVariant::adultInfo), ((MapCodec)AssetInfo.CODEC.fieldOf("baby_assets")).forGetter(WolfVariant::babyInfo)).apply((Applicative<WolfVariant, ?>)i, WolfVariant::new));
    public static final Codec<Holder<WolfVariant>> CODEC = RegistryFixedCodec.create(Registries.WOLF_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_VARIANT);

    private WolfVariant(AssetInfo adultInfo, AssetInfo babyInfo) {
        this(adultInfo, babyInfo, SpawnPrioritySelectors.EMPTY);
    }

    @Override
    public List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors() {
        return this.spawnConditions.selectors();
    }

    public record AssetInfo(ClientAsset.ResourceTexture wild, ClientAsset.ResourceTexture tame, ClientAsset.ResourceTexture angry) {
        public static final Codec<AssetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ClientAsset.ResourceTexture.CODEC.fieldOf("wild")).forGetter(AssetInfo::wild), ((MapCodec)ClientAsset.ResourceTexture.CODEC.fieldOf("tame")).forGetter(AssetInfo::tame), ((MapCodec)ClientAsset.ResourceTexture.CODEC.fieldOf("angry")).forGetter(AssetInfo::angry)).apply((Applicative<AssetInfo, ?>)instance, AssetInfo::new));
    }
}

