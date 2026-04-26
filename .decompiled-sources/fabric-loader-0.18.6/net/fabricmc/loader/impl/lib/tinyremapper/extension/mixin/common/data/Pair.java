/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data;

import java.util.Objects;

public final class Pair<L, R> {
    private final L first;
    private final R second;

    private Pair(L first, R second) {
        this.first = first;
        this.second = second;
    }

    public static <L, R> Pair<L, R> of(L first, R second) {
        return new Pair<L, R>(first, second);
    }

    public L first() {
        return this.first;
    }

    public R second() {
        return this.second;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pair pair = (Pair)o;
        if (!Objects.equals(this.first, pair.first)) {
            return false;
        }
        return Objects.equals(this.second, pair.second);
    }

    public int hashCode() {
        int result = this.first != null ? this.first.hashCode() : 0;
        result = 31 * result + (this.second != null ? this.second.hashCode() : 0);
        return result;
    }
}

