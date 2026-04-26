/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import java.util.Comparator;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public class ActivityComparator
implements Serializable,
Comparator<Constr> {
    @Override
    public int compare(Constr c1, Constr c2) {
        long delta = Math.round(c1.getActivity() - c2.getActivity());
        if (delta == 0L) {
            return c1.size() - c2.size();
        }
        return (int)delta;
    }
}

