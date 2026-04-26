/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.Nullable;

public interface DensityFunction {
    public static final Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
    public static final Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registries.DENSITY_FUNCTION, DIRECT_CODEC);
    public static final Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(DensityFunctions.HolderHolder::new, value -> {
        if (value instanceof DensityFunctions.HolderHolder) {
            DensityFunctions.HolderHolder holder = (DensityFunctions.HolderHolder)value;
            return holder.function();
        }
        return Holder.direct(value);
    });

    public double compute(FunctionContext var1);

    public void fillArray(double[] var1, ContextProvider var2);

    public DensityFunction mapAll(Visitor var1);

    public double minValue();

    public double maxValue();

    public KeyDispatchDataCodec<? extends DensityFunction> codec();

    default public DensityFunction clamp(double min, double max) {
        return new DensityFunctions.Clamp(this, min, max);
    }

    default public DensityFunction abs() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
    }

    default public DensityFunction square() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
    }

    default public DensityFunction cube() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
    }

    default public DensityFunction halfNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
    }

    default public DensityFunction quarterNegative() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
    }

    default public DensityFunction invert() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.INVERT);
    }

    default public DensityFunction squeeze() {
        return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
    }

    public record SinglePointContext(int blockX, int blockY, int blockZ) implements FunctionContext
    {
    }

    public static interface FunctionContext {
        public int blockX();

        public int blockY();

        public int blockZ();

        default public Blender getBlender() {
            return Blender.empty();
        }
    }

    public static interface SimpleFunction
    extends DensityFunction {
        @Override
        default public void fillArray(double[] output, ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(output, this);
        }

        @Override
        default public DensityFunction mapAll(Visitor visitor) {
            return visitor.apply(this);
        }
    }

    public static interface Visitor {
        public DensityFunction apply(DensityFunction var1);

        default public NoiseHolder visitNoise(NoiseHolder noise) {
            return noise;
        }
    }

    public record NoiseHolder(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
        public static final Codec<NoiseHolder> CODEC = NormalNoise.NoiseParameters.CODEC.xmap(data -> new NoiseHolder((Holder<NormalNoise.NoiseParameters>)data, null), NoiseHolder::noiseData);

        public NoiseHolder(Holder<NormalNoise.NoiseParameters> noiseData) {
            this(noiseData, null);
        }

        public double getValue(double x, double y, double z) {
            return this.noise == null ? 0.0 : this.noise.getValue(x, y, z);
        }

        public double maxValue() {
            return this.noise == null ? 2.0 : this.noise.maxValue();
        }
    }

    public static interface ContextProvider {
        public FunctionContext forIndex(int var1);

        public void fillAllDirectly(double[] var1, DensityFunction var2);
    }
}

