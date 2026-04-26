/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionInterval;

public final class VersionIntervalImpl
implements VersionInterval {
    private final Version min;
    private final boolean minInclusive;
    private final Version max;
    private final boolean maxInclusive;

    public VersionIntervalImpl(Version min, boolean minInclusive, Version max, boolean maxInclusive) {
        this.min = min;
        this.minInclusive = min != null ? minInclusive : false;
        this.max = max;
        boolean bl = this.maxInclusive = max != null ? maxInclusive : false;
        assert (min != null || !minInclusive);
        assert (max != null || !maxInclusive);
        assert (min == null || min instanceof SemanticVersion || minInclusive);
        assert (max == null || max instanceof SemanticVersion || maxInclusive);
        assert (min == null || max == null || min instanceof SemanticVersion && max instanceof SemanticVersion || min.equals(max));
    }

    @Override
    public boolean isSemantic() {
        return !(this.min != null && !(this.min instanceof SemanticVersion) || this.max != null && !(this.max instanceof SemanticVersion));
    }

    @Override
    public Version getMin() {
        return this.min;
    }

    @Override
    public boolean isMinInclusive() {
        return this.minInclusive;
    }

    @Override
    public Version getMax() {
        return this.max;
    }

    @Override
    public boolean isMaxInclusive() {
        return this.maxInclusive;
    }

    public boolean equals(Object obj) {
        if (obj instanceof VersionInterval) {
            VersionInterval o = (VersionInterval)obj;
            return Objects.equals(this.min, o.getMin()) && this.minInclusive == o.isMinInclusive() && Objects.equals(this.max, o.getMax()) && this.maxInclusive == o.isMaxInclusive();
        }
        return false;
    }

    public int hashCode() {
        return (Objects.hashCode(this.min) + (this.minInclusive ? 1 : 0)) * 31 + (Objects.hashCode(this.max) + (this.maxInclusive ? 1 : 0)) * 31;
    }

    public String toString() {
        if (this.min == null) {
            if (this.max == null) {
                return "(-\u221e,\u221e)";
            }
            return String.format("(-\u221e,%s%c", this.max, Character.valueOf(this.maxInclusive ? (char)']' : ')'));
        }
        if (this.max == null) {
            return String.format("%c%s,\u221e)", Character.valueOf(this.minInclusive ? (char)'[' : '('), this.min);
        }
        return String.format("%c%s,%s%c", Character.valueOf(this.minInclusive ? (char)'[' : '('), this.min, this.max, Character.valueOf(this.maxInclusive ? (char)']' : ')'));
    }

    public static VersionInterval and(VersionInterval a, VersionInterval b) {
        if (a == null || b == null) {
            return null;
        }
        if (!a.isSemantic() || !b.isSemantic()) {
            return VersionIntervalImpl.andPlain(a, b);
        }
        return VersionIntervalImpl.andSemantic(a, b);
    }

    private static VersionInterval andPlain(VersionInterval a, VersionInterval b) {
        Version aMin = a.getMin();
        Version aMax = a.getMax();
        Version bMin = b.getMin();
        Version bMax = b.getMax();
        if (aMin != null) {
            if (bMin != null && !aMin.equals(bMin) || bMax != null && !aMin.equals(bMax)) {
                return null;
            }
            if (aMax != null || bMax == null) {
                assert (Objects.equals(aMax, bMax) || bMax == null);
                return a;
            }
            return new VersionIntervalImpl(aMin, true, bMax, b.isMaxInclusive());
        }
        if (aMax != null) {
            if (bMin != null && !aMax.equals(bMin) || bMax != null && !aMax.equals(bMax)) {
                return null;
            }
            if (bMin == null) {
                return a;
            }
            if (bMax != null) {
                return b;
            }
            return new VersionIntervalImpl(bMin, true, aMax, true);
        }
        return b;
    }

    private static VersionInterval andSemantic(VersionInterval a, VersionInterval b) {
        SemanticVersion bMax;
        int minCmp = VersionIntervalImpl.compareMin(a, b);
        int maxCmp = VersionIntervalImpl.compareMax(a, b);
        if (minCmp == 0) {
            if (maxCmp == 0) {
                return a;
            }
            return maxCmp < 0 ? a : b;
        }
        if (maxCmp == 0) {
            return minCmp < 0 ? b : a;
        }
        if (minCmp < 0) {
            if (maxCmp > 0) {
                return b;
            }
            SemanticVersion aMax = (SemanticVersion)a.getMax();
            SemanticVersion bMin = (SemanticVersion)b.getMin();
            int cmp = bMin.compareTo(aMax);
            if (cmp < 0 || cmp == 0 && b.isMinInclusive() && a.isMaxInclusive()) {
                return new VersionIntervalImpl(bMin, b.isMinInclusive(), aMax, a.isMaxInclusive());
            }
            return null;
        }
        if (maxCmp < 0) {
            return a;
        }
        SemanticVersion aMin = (SemanticVersion)a.getMin();
        int cmp = aMin.compareTo(bMax = (SemanticVersion)b.getMax());
        if (cmp < 0 || cmp == 0 && a.isMinInclusive() && b.isMaxInclusive()) {
            return new VersionIntervalImpl(aMin, a.isMinInclusive(), bMax, b.isMaxInclusive());
        }
        return null;
    }

    public static List<VersionInterval> and(Collection<VersionInterval> a, Collection<VersionInterval> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return Collections.emptyList();
        }
        if (a.size() == 1 && b.size() == 1) {
            VersionInterval merged = VersionIntervalImpl.and(a.iterator().next(), b.iterator().next());
            return merged != null ? Collections.singletonList(merged) : Collections.emptyList();
        }
        ArrayList<VersionInterval> allMerged = new ArrayList<VersionInterval>();
        for (VersionInterval intervalA : a) {
            for (VersionInterval intervalB : b) {
                VersionInterval merged = VersionIntervalImpl.and(intervalA, intervalB);
                if (merged == null) continue;
                allMerged.add(merged);
            }
        }
        if (allMerged.isEmpty()) {
            return Collections.emptyList();
        }
        if (allMerged.size() == 1) {
            return allMerged;
        }
        ArrayList<VersionInterval> ret = new ArrayList<VersionInterval>(allMerged.size());
        for (VersionInterval v : allMerged) {
            VersionIntervalImpl.merge(v, ret);
        }
        return ret;
    }

    public static List<VersionInterval> or(Collection<VersionInterval> a, VersionInterval b) {
        if (a.isEmpty()) {
            if (b == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(b);
        }
        ArrayList<VersionInterval> ret = new ArrayList<VersionInterval>(a.size() + 1);
        for (VersionInterval v : a) {
            VersionIntervalImpl.merge(v, ret);
        }
        VersionIntervalImpl.merge(b, ret);
        return ret;
    }

    private static void merge(VersionInterval a, List<VersionInterval> out) {
        VersionInterval e;
        if (a == null) {
            return;
        }
        if (out.isEmpty()) {
            out.add(a);
            return;
        }
        if (out.size() == 1 && (e = out.get(0)).getMin() == null && e.getMax() == null) {
            return;
        }
        if (!a.isSemantic()) {
            VersionIntervalImpl.mergePlain(a, out);
        } else {
            VersionIntervalImpl.mergeSemantic(a, out);
        }
    }

    private static void mergePlain(VersionInterval a, List<VersionInterval> out) {
        Version v;
        Version aMin = a.getMin();
        Version aMax = a.getMax();
        Version version = v = aMin != null ? aMin : aMax;
        assert (v != null);
        for (int i = 0; i < out.size(); ++i) {
            VersionInterval c = out.get(i);
            if (v.equals(c.getMin())) {
                if (aMin == null) {
                    assert (aMax.equals(c.getMin()));
                    out.clear();
                    out.add(INFINITE);
                } else if (aMax == null && c.getMax() != null) {
                    out.set(i, a);
                }
                return;
            }
            if (!v.equals(c.getMax())) continue;
            assert (c.getMin() == null);
            if (aMax == null) {
                assert (aMin.equals(c.getMax()));
                out.clear();
                out.add(INFINITE);
            }
            return;
        }
        out.add(a);
    }

    private static void mergeSemantic(VersionInterval a, List<VersionInterval> out) {
        SemanticVersion aMin = (SemanticVersion)a.getMin();
        SemanticVersion aMax = (SemanticVersion)a.getMax();
        if (aMin == null && aMax == null) {
            out.clear();
            out.add(INFINITE);
            return;
        }
        for (int i = 0; i < out.size(); ++i) {
            int cmp2;
            int cmp;
            VersionInterval c = out.get(i);
            if (!c.isSemantic()) continue;
            SemanticVersion cMin = (SemanticVersion)c.getMin();
            SemanticVersion cMax = (SemanticVersion)c.getMax();
            if (aMin == null) {
                if (cMax == null) {
                    cmp = aMax.compareTo(cMin);
                    if (cmp < 0 || cmp == 0 && !a.isMaxInclusive() && !c.isMinInclusive()) {
                        out.add(i, a);
                    } else {
                        out.clear();
                        out.add(INFINITE);
                    }
                    return;
                }
                cmp = VersionIntervalImpl.compareMax(a, c);
                if (cmp >= 0) {
                    out.remove(i);
                    --i;
                    continue;
                }
                if (cMin == null) {
                    return;
                }
                cmp = aMax.compareTo(cMin);
                if (cmp < 0 || cmp == 0 && !a.isMaxInclusive() && !c.isMinInclusive()) {
                    out.add(i, a);
                } else {
                    out.set(i, new VersionIntervalImpl(null, false, cMax, c.isMaxInclusive()));
                }
                return;
            }
            if (cMax == null) {
                cmp = VersionIntervalImpl.compareMin(a, c);
                if (cmp < 0) {
                    if (aMax == null) {
                        while (out.size() > i) {
                            out.remove(i);
                        }
                        out.add(a);
                    } else {
                        cmp = aMax.compareTo(cMin);
                        if (cmp < 0 || cmp == 0 && !a.isMaxInclusive() && !c.isMinInclusive()) {
                            out.add(i, a);
                        } else {
                            out.set(i, new VersionIntervalImpl(aMin, a.isMinInclusive(), null, false));
                        }
                    }
                }
                return;
            }
            cmp = aMin.compareTo(cMax);
            if (cmp >= 0 && (cmp != 0 || !a.isMinInclusive() && !c.isMaxInclusive())) continue;
            if (aMax == null || cMin == null || (cmp2 = aMax.compareTo(cMin)) > 0 || cmp2 == 0 && (a.isMaxInclusive() || c.isMinInclusive())) {
                int cmpMin = VersionIntervalImpl.compareMin(a, c);
                int cmpMax = VersionIntervalImpl.compareMax(a, c);
                if (cmpMax <= 0) {
                    if (cmpMin < 0) {
                        out.set(i, new VersionIntervalImpl(aMin, a.isMinInclusive(), cMax, c.isMaxInclusive()));
                    }
                    return;
                }
                if (cmpMin > 0) {
                    a = new VersionIntervalImpl(cMin, c.isMinInclusive(), aMax, a.isMaxInclusive());
                }
                out.remove(i);
                --i;
                continue;
            }
            out.add(i, a);
            return;
        }
        out.add(a);
    }

    private static int compareMin(VersionInterval a, VersionInterval b) {
        int cmp;
        SemanticVersion aMin = (SemanticVersion)a.getMin();
        SemanticVersion bMin = (SemanticVersion)b.getMin();
        if (aMin == null) {
            if (bMin == null) {
                return 0;
            }
            return -1;
        }
        if (bMin == null || (cmp = aMin.compareTo(bMin)) > 0 || cmp == 0 && !a.isMinInclusive() && b.isMinInclusive()) {
            return 1;
        }
        if (cmp < 0 || a.isMinInclusive() && !b.isMinInclusive()) {
            return -1;
        }
        return 0;
    }

    private static int compareMax(VersionInterval a, VersionInterval b) {
        int cmp;
        SemanticVersion aMax = (SemanticVersion)a.getMax();
        SemanticVersion bMax = (SemanticVersion)b.getMax();
        if (aMax == null) {
            if (bMax == null) {
                return 0;
            }
            return 1;
        }
        if (bMax == null || (cmp = aMax.compareTo(bMax)) < 0 || cmp == 0 && !a.isMaxInclusive() && b.isMaxInclusive()) {
            return -1;
        }
        if (cmp > 0 || a.isMaxInclusive() && !b.isMaxInclusive()) {
            return 1;
        }
        return 0;
    }

    public static List<VersionInterval> not(VersionInterval interval) {
        if (interval == null) {
            return Collections.singletonList(INFINITE);
        }
        if (interval.getMin() == null) {
            if (interval.getMax() == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new VersionIntervalImpl(interval.getMax(), !interval.isMaxInclusive(), null, false));
        }
        if (interval.getMax() == null) {
            return Collections.singletonList(new VersionIntervalImpl(null, false, interval.getMin(), !interval.isMinInclusive()));
        }
        if (interval.getMin().equals(interval.getMax()) && !interval.isMinInclusive() && !interval.isMaxInclusive()) {
            return Collections.singletonList(INFINITE);
        }
        ArrayList<VersionInterval> ret = new ArrayList<VersionInterval>(2);
        ret.add(new VersionIntervalImpl(null, false, interval.getMin(), !interval.isMinInclusive()));
        ret.add(new VersionIntervalImpl(interval.getMax(), !interval.isMaxInclusive(), null, false));
        return ret;
    }

    public static List<VersionInterval> not(Collection<VersionInterval> intervals) {
        if (intervals.isEmpty()) {
            return Collections.singletonList(INFINITE);
        }
        if (intervals.size() == 1) {
            return VersionIntervalImpl.not(intervals.iterator().next());
        }
        List<VersionInterval> ret = null;
        for (VersionInterval v : intervals) {
            List<VersionInterval> inverted = VersionIntervalImpl.not(v);
            if (!(ret = ret == null ? inverted : VersionIntervalImpl.and(ret, inverted)).isEmpty()) continue;
            break;
        }
        return ret;
    }
}

