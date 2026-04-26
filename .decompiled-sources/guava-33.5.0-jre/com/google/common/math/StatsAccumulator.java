/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.math;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.math.DoubleUtils;
import com.google.common.math.Stats;
import java.util.Iterator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@J2ktIncompatible
@GwtIncompatible
public final class StatsAccumulator {
    private long count = 0L;
    private double mean = 0.0;
    private double sumOfSquaresOfDeltas = 0.0;
    private double min = Double.NaN;
    private double max = Double.NaN;

    public void add(double value) {
        if (this.count == 0L) {
            this.count = 1L;
            this.mean = value;
            this.min = value;
            this.max = value;
            if (!Double.isFinite(value)) {
                this.sumOfSquaresOfDeltas = Double.NaN;
            }
        } else {
            ++this.count;
            if (Double.isFinite(value) && Double.isFinite(this.mean)) {
                double delta = value - this.mean;
                this.mean += delta / (double)this.count;
                this.sumOfSquaresOfDeltas += delta * (value - this.mean);
            } else {
                this.mean = StatsAccumulator.calculateNewMeanNonFinite(this.mean, value);
                this.sumOfSquaresOfDeltas = Double.NaN;
            }
            this.min = Math.min(this.min, value);
            this.max = Math.max(this.max, value);
        }
    }

    public void addAll(Iterable<? extends Number> values) {
        for (Number number : values) {
            this.add(number.doubleValue());
        }
    }

    public void addAll(Iterator<? extends Number> values) {
        while (values.hasNext()) {
            this.add(values.next().doubleValue());
        }
    }

    public void addAll(double ... values) {
        for (double value : values) {
            this.add(value);
        }
    }

    public void addAll(int ... values) {
        for (int value : values) {
            this.add(value);
        }
    }

    public void addAll(long ... values) {
        for (long value : values) {
            this.add(value);
        }
    }

    public void addAll(DoubleStream values) {
        this.addAll(values.collect(StatsAccumulator::new, StatsAccumulator::add, StatsAccumulator::addAll));
    }

    public void addAll(IntStream values) {
        this.addAll(values.collect(StatsAccumulator::new, StatsAccumulator::add, StatsAccumulator::addAll));
    }

    public void addAll(LongStream values) {
        this.addAll(values.collect(StatsAccumulator::new, StatsAccumulator::add, StatsAccumulator::addAll));
    }

    public void addAll(Stats values) {
        if (values.count() == 0L) {
            return;
        }
        this.merge(values.count(), values.mean(), values.sumOfSquaresOfDeltas(), values.min(), values.max());
    }

    public void addAll(StatsAccumulator values) {
        if (values.count() == 0L) {
            return;
        }
        this.merge(values.count(), values.mean(), values.sumOfSquaresOfDeltas(), values.min(), values.max());
    }

    private void merge(long otherCount, double otherMean, double otherSumOfSquaresOfDeltas, double otherMin, double otherMax) {
        if (this.count == 0L) {
            this.count = otherCount;
            this.mean = otherMean;
            this.sumOfSquaresOfDeltas = otherSumOfSquaresOfDeltas;
            this.min = otherMin;
            this.max = otherMax;
        } else {
            this.count += otherCount;
            if (Double.isFinite(this.mean) && Double.isFinite(otherMean)) {
                double delta = otherMean - this.mean;
                this.mean += delta * (double)otherCount / (double)this.count;
                this.sumOfSquaresOfDeltas += otherSumOfSquaresOfDeltas + delta * (otherMean - this.mean) * (double)otherCount;
            } else {
                this.mean = StatsAccumulator.calculateNewMeanNonFinite(this.mean, otherMean);
                this.sumOfSquaresOfDeltas = Double.NaN;
            }
            this.min = Math.min(this.min, otherMin);
            this.max = Math.max(this.max, otherMax);
        }
    }

    public Stats snapshot() {
        return new Stats(this.count, this.mean, this.sumOfSquaresOfDeltas, this.min, this.max);
    }

    public long count() {
        return this.count;
    }

    public double mean() {
        Preconditions.checkState(this.count != 0L);
        return this.mean;
    }

    public final double sum() {
        return this.mean * (double)this.count;
    }

    public final double populationVariance() {
        Preconditions.checkState(this.count != 0L);
        if (Double.isNaN(this.sumOfSquaresOfDeltas)) {
            return Double.NaN;
        }
        if (this.count == 1L) {
            return 0.0;
        }
        return DoubleUtils.ensureNonNegative(this.sumOfSquaresOfDeltas) / (double)this.count;
    }

    public final double populationStandardDeviation() {
        return Math.sqrt(this.populationVariance());
    }

    public final double sampleVariance() {
        Preconditions.checkState(this.count > 1L);
        if (Double.isNaN(this.sumOfSquaresOfDeltas)) {
            return Double.NaN;
        }
        return DoubleUtils.ensureNonNegative(this.sumOfSquaresOfDeltas) / (double)(this.count - 1L);
    }

    public final double sampleStandardDeviation() {
        return Math.sqrt(this.sampleVariance());
    }

    public double min() {
        Preconditions.checkState(this.count != 0L);
        return this.min;
    }

    public double max() {
        Preconditions.checkState(this.count != 0L);
        return this.max;
    }

    double sumOfSquaresOfDeltas() {
        return this.sumOfSquaresOfDeltas;
    }

    static double calculateNewMeanNonFinite(double previousMean, double value) {
        if (Double.isFinite(previousMean)) {
            return value;
        }
        if (Double.isFinite(value) || previousMean == value) {
            return previousMean;
        }
        return Double.NaN;
    }
}

