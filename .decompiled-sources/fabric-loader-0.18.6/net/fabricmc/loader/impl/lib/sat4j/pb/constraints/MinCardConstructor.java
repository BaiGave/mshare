/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints;

import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.card.MinWatchCard;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.ICardConstructor;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public class MinCardConstructor
implements ICardConstructor {
    @Override
    public Constr constructCard(UnitPropagationListener solver, ILits voc, IVecInt theLits, int degree) throws ContradictionException {
        Constr constr = MinWatchCard.minWatchCardNew(solver, voc, theLits, true, degree);
        if (constr == null) {
            return Constr.TAUTOLOGY;
        }
        return constr;
    }
}

