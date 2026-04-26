/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public final class WeightedPicker<T> {
    private double currentTotal;
    private final List<WeightedEntry<T>> entries;

    WeightedPicker() {
        this(0.0, new ArrayList<WeightedEntry<T>>());
    }

    private WeightedPicker(double currentTotal, List<WeightedEntry<T>> entries) {
        this.currentTotal = currentTotal;
        this.entries = entries;
    }

    void add(T biome, double weight) {
        this.currentTotal += weight;
        this.entries.add(new WeightedEntry<T>(biome, weight, this.currentTotal));
    }

    double getCurrentWeightTotal() {
        return this.currentTotal;
    }

    int getEntryCount() {
        return this.entries.size();
    }

    public T pickFromNoise(ImprovedNoise sampler, double x, double y, double z) {
        double target = Mth.clamp(Math.abs(sampler.noise(x, y, z)), 0.0, 1.0) * this.getCurrentWeightTotal();
        return this.search(target).entry();
    }

    <U> WeightedPicker<U> map(Function<T, U> mapper) {
        return new WeightedPicker<T>(this.currentTotal, this.entries.stream().map((? super T e) -> new WeightedEntry(mapper.apply(e.entry), e.weight, e.upperWeightBound)).toList());
    }

    WeightedEntry<T> search(double target) {
        Preconditions.checkArgument(target <= this.currentTotal, "The provided target value for entry selection must be less than or equal to the weight total");
        Preconditions.checkArgument(target >= 0.0, "The provided target value for entry selection cannot be negative");
        int low = 0;
        int high = this.entries.size() - 1;
        while (low < high) {
            int mid = high + low >>> 1;
            if (target < this.entries.get(mid).upperWeightBound()) {
                high = mid;
                continue;
            }
            low = mid + 1;
        }
        return this.entries.get(low);
    }

    record WeightedEntry<T>(T entry, double weight, double upperWeightBound) {
    }
}

