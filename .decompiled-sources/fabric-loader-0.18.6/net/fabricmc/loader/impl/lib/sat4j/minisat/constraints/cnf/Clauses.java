/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public abstract class Clauses {
    public static IVecInt sanityCheck(IVecInt ps, ILits voc, UnitPropagationListener s) throws ContradictionException {
        int i = 0;
        while (i < ps.size()) {
            if (voc.isUnassigned(ps.get(i))) {
                ++i;
                continue;
            }
            if (voc.isSatisfied(ps.get(i))) {
                return null;
            }
            ps.delete(i);
        }
        ps.sortUnique();
        for (i = 0; i < ps.size() - 1; ++i) {
            if (ps.get(i) != (ps.get(i + 1) ^ 1)) continue;
            return null;
        }
        Clauses.propagationCheck(ps, s);
        return ps;
    }

    static boolean propagationCheck(IVecInt ps, UnitPropagationListener s) throws ContradictionException {
        if (ps.size() == 0) {
            throw new ContradictionException("Creating Empty clause ?");
        }
        if (ps.size() == 1) {
            if (!s.enqueue(ps.get(0))) {
                throw new ContradictionException("Contradictory Unit Clauses");
            }
            return true;
        }
        return false;
    }
}

