/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.tools;

import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolver;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.tools.SolverDecorator;

public class GateTranslator
extends SolverDecorator<ISolver> {
    public GateTranslator(ISolver solver) {
        super(solver);
    }

    public IConstr gateFalse(int y) throws ContradictionException {
        VecInt clause = new VecInt(2);
        clause.push(-y);
        return this.processClause(clause);
    }

    public IConstr gateTrue(int y) throws ContradictionException {
        VecInt clause = new VecInt(2);
        clause.push(y);
        return this.processClause(clause);
    }

    private IConstr processClause(IVecInt clause) throws ContradictionException {
        return this.addClause(clause);
    }
}

