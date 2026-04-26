/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.DataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.GlucoseLCDS;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Solver;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public class Glucose2LCDS<D extends DataStructureFactory>
extends GlucoseLCDS<D> {
    protected Glucose2LCDS(Solver<D> solver, ConflictTimer timer) {
        super(solver, timer);
    }

    @Override
    public String toString() {
        return "Glucose 2 learned constraints deletion strategy (LBD updated on propagation) with timer " + this.getTimer();
    }

    @Override
    public void onPropagation(Constr from, int propagated) {
        int nblevel;
        if (from.getActivity() > 2.0 && (double)(nblevel = this.computeLBD(from, propagated)) < from.getActivity()) {
            this.getSolver().stats.incUpdateLBD();
            from.setActivity(nblevel);
        }
    }
}

