/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.tools;

import java.math.BigInteger;

public final class WeightedObject<T>
implements Comparable<WeightedObject<T>> {
    public final T thing;
    private BigInteger weight;

    private WeightedObject(T thing, BigInteger weight) {
        this.thing = thing;
        this.weight = weight;
    }

    public BigInteger getWeight() {
        return this.weight;
    }

    @Override
    public int compareTo(WeightedObject<T> arg0) {
        return this.weight.compareTo(arg0.getWeight());
    }

    public static <E> WeightedObject<E> newWO(E e, BigInteger w) {
        return new WeightedObject<E>(e, w);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.thing == null ? 0 : this.thing.hashCode());
        result = 31 * result + (this.weight == null ? 0 : this.weight.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        WeightedObject other = (WeightedObject)obj;
        if (this.thing == null ? other.thing != null : !this.thing.equals(other.thing)) {
            return false;
        }
        return !(this.weight == null ? other.weight != null : !this.weight.equals(other.weight));
    }
}

