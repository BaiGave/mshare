/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;

public class LinearPosTest
extends PosRuleTest {
    public static final MapCodec<LinearPosTest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("min_chance")).orElse(Float.valueOf(0.0f)).forGetter(p -> Float.valueOf(p.minChance)), ((MapCodec)Codec.FLOAT.fieldOf("max_chance")).orElse(Float.valueOf(0.0f)).forGetter(p -> Float.valueOf(p.maxChance)), ((MapCodec)Codec.INT.fieldOf("min_dist")).orElse(0).forGetter(p -> p.minDist), ((MapCodec)Codec.INT.fieldOf("max_dist")).orElse(0).forGetter(p -> p.maxDist)).apply((Applicative<LinearPosTest, ?>)i, LinearPosTest::new));
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;

    public LinearPosTest(float minChance, float maxChance, int minDist, int maxDist) {
        if (minDist >= maxDist) {
            throw new IllegalArgumentException("Invalid range: [" + minDist + "," + maxDist + "]");
        }
        this.minChance = minChance;
        this.maxChance = maxChance;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    public boolean test(BlockPos inTemplatePos, BlockPos worldPos, BlockPos worldReference, RandomSource random) {
        int dist = worldPos.distManhattan(worldReference);
        float rnd = random.nextFloat();
        return rnd <= Mth.clampedLerp(Mth.inverseLerp(dist, this.minDist, this.maxDist), this.minChance, this.maxChance);
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }
}

