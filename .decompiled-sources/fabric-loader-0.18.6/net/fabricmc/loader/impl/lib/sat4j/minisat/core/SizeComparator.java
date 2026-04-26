/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import java.util.Comparator;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public class SizeComparator
implements Serializable,
Comparator<Constr> {
    @Override
    public int compare(Constr c1, Constr c2) {
        int delta = c1.size() - c2.size();
        if (delta == 0) {
            return (int)Math.round(c2.getActivity() - c1.getActivity());
        }
        return delta;
    }
}

