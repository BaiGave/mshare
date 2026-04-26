/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public interface VerticalAnchor {
    public static final Codec<VerticalAnchor> CODEC = Codec.xor(Absolute.CODEC, Codec.xor(AboveBottom.CODEC, BelowTop.CODEC)).xmap(VerticalAnchor::merge, VerticalAnchor::split);
    public static final VerticalAnchor BOTTOM = VerticalAnchor.aboveBottom(0);
    public static final VerticalAnchor TOP = VerticalAnchor.belowTop(0);

    public static VerticalAnchor absolute(int value) {
        return new Absolute(value);
    }

    public static VerticalAnchor aboveBottom(int offset) {
        return new AboveBottom(offset);
    }

    public static VerticalAnchor belowTop(int offset) {
        return new BelowTop(offset);
    }

    public static VerticalAnchor bottom() {
        return BOTTOM;
    }

    public static VerticalAnchor top() {
        return TOP;
    }

    private static VerticalAnchor merge(Either<Absolute, Either<AboveBottom, BelowTop>> either) {
        return (VerticalAnchor)((Object)either.map(Function.identity(), Either::unwrap));
    }

    private static Either<Absolute, Either<AboveBottom, BelowTop>> split(VerticalAnchor anchor) {
        if (anchor instanceof Absolute) {
            return Either.left((Absolute)anchor);
        }
        return Either.right(anchor instanceof AboveBottom ? Either.left((AboveBottom)anchor) : Either.right((BelowTop)anchor));
    }

    public int resolveY(WorldGenerationContext var1);

    public record Absolute(int y) implements VerticalAnchor
    {
        public static final Codec<Absolute> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("absolute")).xmap(Absolute::new, Absolute::y).codec();

        @Override
        public int resolveY(WorldGenerationContext heightAccessor) {
            return this.y;
        }

        @Override
        public String toString() {
            return this.y + " absolute";
        }
    }

    public record AboveBottom(int offset) implements VerticalAnchor
    {
        public static final Codec<AboveBottom> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("above_bottom")).xmap(AboveBottom::new, AboveBottom::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext heightAccessor) {
            return heightAccessor.getMinGenY() + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " above bottom";
        }
    }

    public record BelowTop(int offset) implements VerticalAnchor
    {
        public static final Codec<BelowTop> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("below_top")).xmap(BelowTop::new, BelowTop::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext heightAccessor) {
            return heightAccessor.getGenDepth() - 1 + heightAccessor.getMinGenY() - this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " below top";
        }
    }
}

