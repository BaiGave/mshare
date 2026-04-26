/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlock
extends BaseTorchBlock {
    protected static final MapCodec<SimpleParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE.byNameCodec().comapFlatMap(type -> {
        DataResult<Object> dataResult;
        if (type instanceof SimpleParticleType) {
            SimpleParticleType simple = (SimpleParticleType)type;
            dataResult = DataResult.success(simple);
        } else {
            dataResult = DataResult.error(() -> "Not a SimpleParticleType: " + String.valueOf(type));
        }
        return dataResult;
    }, type -> type).fieldOf("particle_options");
    public static final MapCodec<TorchBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(PARTICLE_OPTIONS_FIELD.forGetter(b -> b.flameParticle), TorchBlock.propertiesCodec()).apply((Applicative<TorchBlock, ?>)i, TorchBlock::new));
    protected final SimpleParticleType flameParticle;

    public MapCodec<? extends TorchBlock> codec() {
        return CODEC;
    }

    public TorchBlock(SimpleParticleType flameParticle, BlockBehaviour.Properties properties) {
        super(properties);
        this.flameParticle = flameParticle;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.7;
        double z = (double)pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(this.flameParticle, x, y, z, 0.0, 0.0, 0.0);
    }
}

