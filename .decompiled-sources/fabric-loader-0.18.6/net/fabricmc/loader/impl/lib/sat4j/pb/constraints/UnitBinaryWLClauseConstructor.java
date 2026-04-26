/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints;

import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.LearntBinaryClause;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.LearntWLClause;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.OriginalBinaryClause;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.OriginalWLClause;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.UnitClause;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.IClauseConstructor;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public class UnitBinaryWLClauseConstructor
implements IClauseConstructor {
    @Override
    public Constr constructClause(UnitPropagationListener solver, ILits voc, IVecInt v) {
        if (v == null) {
            return Constr.TAUTOLOGY;
        }
        if (v.size() == 1) {
            return new UnitClause(v.last());
        }
        if (v.size() == 2) {
            return OriginalBinaryClause.brandNewClause(solver, voc, v);
        }
        return OriginalWLClause.brandNewClause(solver, voc, v);
    }

    @Override
    public Constr constructLearntClause(ILits voc, IVecInt literals) {
        if (literals.size() == 1) {
            return new UnitClause(literals.last(), true);
        }
        if (literals.size() == 2) {
            return new LearntBinaryClause(literals, voc);
        }
        return new LearntWLClause(literals, voc);
    }
}

