/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class RandomBlockStateMatchTest
extends RuleTest {
    public static final MapCodec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BlockState.CODEC.fieldOf("block_state")).forGetter(t -> t.blockState), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(t -> Float.valueOf(t.probability))).apply((Applicative<RandomBlockStateMatchTest, ?>)i, RandomBlockStateMatchTest::new));
    private final BlockState blockState;
    private final float probability;

    public RandomBlockStateMatchTest(BlockState blockState, float probability) {
        this.blockState = blockState;
        this.probability = probability;
    }

    @Override
    public boolean test(BlockState blockState, RandomSource random) {
        return blockState == this.blockState && random.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCKSTATE_TEST;
    }
}

