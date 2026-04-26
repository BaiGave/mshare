/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

final class Bang<A>
extends PointFree<Function<A, Unit>> {
    private final Type<A> type;

    Bang(Type<A> type) {
        this.type = type;
    }

    @Override
    public Type<Function<A, Unit>> type() {
        return DSL.func(this.type, DSL.emptyPartType());
    }

    @Override
    public String toString(int level) {
        return "!";
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (!(o instanceof Bang)) return false;
        Bang bang = (Bang)o;
        if (!this.type.equals(bang.type)) return false;
        return true;
    }

    public int hashCode() {
        return this.type.hashCode();
    }

    @Override
    public Function<DynamicOps<?>, Function<A, Unit>> eval() {
        return ops -> a -> Unit.INSTANCE;
    }
}

