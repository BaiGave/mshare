/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb;

import java.math.BigInteger;
import net.fabricmc.loader.impl.lib.sat4j.pb.IPBSolver;
import net.fabricmc.loader.impl.lib.sat4j.pb.ObjectiveFunction;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVec;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.tools.SolverDecorator;

public class PBSolverDecorator
extends SolverDecorator<IPBSolver>
implements IPBSolver {
    public PBSolverDecorator(IPBSolver solver) {
        super(solver);
    }

    @Override
    public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs, boolean moreThan, BigInteger d) throws ContradictionException {
        return ((IPBSolver)this.decorated()).addPseudoBoolean(lits, coeffs, moreThan, d);
    }

    @Override
    public void setObjectiveFunction(ObjectiveFunction obj) {
        ((IPBSolver)this.decorated()).setObjectiveFunction(obj);
    }

    @Override
    public ObjectiveFunction getObjectiveFunction() {
        return ((IPBSolver)this.decorated()).getObjectiveFunction();
    }

    @Override
    public IConstr addAtMost(IVecInt literals, IVecInt coeffs, int degree) throws ContradictionException {
        return ((IPBSolver)this.decorated()).addAtMost(literals, coeffs, degree);
    }

    @Override
    public IConstr addAtLeast(IVecInt literals, IVecInt coeffs, int degree) throws ContradictionException {
        return ((IPBSolver)this.decorated()).addAtLeast(literals, coeffs, degree);
    }
}

