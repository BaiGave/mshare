/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jspecify.annotations.Nullable;

public class EndSpikeConfiguration
implements FeatureConfiguration {
    public static final Codec<EndSpikeConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.BOOL.fieldOf("crystal_invulnerable")).orElse(false).forGetter(c -> c.crystalInvulnerable), ((MapCodec)EndSpikeFeature.EndSpike.CODEC.listOf().fieldOf("spikes")).forGetter(c -> c.spikes), BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter(c -> Optional.ofNullable(c.crystalBeamTarget))).apply((Applicative<EndSpikeConfiguration, ?>)i, EndSpikeConfiguration::new));
    private final boolean crystalInvulnerable;
    private final List<EndSpikeFeature.EndSpike> spikes;
    private final @Nullable BlockPos crystalBeamTarget;

    public EndSpikeConfiguration(boolean crystalInvulnerable, List<EndSpikeFeature.EndSpike> spikes, @Nullable BlockPos crystalBeamTarget) {
        this(crystalInvulnerable, spikes, Optional.ofNullable(crystalBeamTarget));
    }

    private EndSpikeConfiguration(boolean crystalInvulnerable, List<EndSpikeFeature.EndSpike> spikes, Optional<BlockPos> crystalBeamTarget) {
        this.crystalInvulnerable = crystalInvulnerable;
        this.spikes = spikes;
        this.crystalBeamTarget = crystalBeamTarget.orElse(null);
    }

    public boolean isCrystalInvulnerable() {
        return this.crystalInvulnerable;
    }

    public List<EndSpikeFeature.EndSpike> getSpikes() {
        return this.spikes;
    }

    public @Nullable BlockPos getCrystalBeamTarget() {
        return this.crystalBeamTarget;
    }
}

