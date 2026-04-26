/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr;

import com.google.common.math.Quantiles;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMaps;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.util.Util;

public class Percentiles {
    public static final Quantiles.ScaleAndIndexes DEFAULT_INDEXES = Quantiles.scale(100).indexes(50, 75, 90, 99);

    private Percentiles() {
    }

    public static Map<Integer, Double> evaluate(long[] dataset) {
        return dataset.length == 0 ? Map.of() : Percentiles.sorted(DEFAULT_INDEXES.compute(dataset));
    }

    public static Map<Integer, Double> evaluate(int[] dataset) {
        return dataset.length == 0 ? Map.of() : Percentiles.sorted(DEFAULT_INDEXES.compute(dataset));
    }

    public static Map<Integer, Double> evaluate(double[] dataset) {
        return dataset.length == 0 ? Map.of() : Percentiles.sorted(DEFAULT_INDEXES.compute(dataset));
    }

    private static Map<Integer, Double> sorted(Map<Integer, Double> percentiles) {
        Int2DoubleSortedMap sorted = Util.make(new Int2DoubleRBTreeMap(Comparator.reverseOrder()), it -> it.putAll((Map<? extends Integer, ? extends Double>)percentiles));
        return Int2DoubleSortedMaps.unmodifiable(sorted);
    }
}

