/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.tools.xplain;

import java.util.ArrayList;
import java.util.Collection;
import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolver;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.IteratorInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;
import net.fabricmc.loader.impl.lib.sat4j.tools.FullClauseSelectorSolver;
import net.fabricmc.loader.impl.lib.sat4j.tools.SolverDecorator;
import net.fabricmc.loader.impl.lib.sat4j.tools.xplain.DeletionStrategy;
import net.fabricmc.loader.impl.lib.sat4j.tools.xplain.MinimizationStrategy;

public class Xplain<T extends ISolver>
extends FullClauseSelectorSolver<T> {
    private IVecInt assump;
    private MinimizationStrategy xplainStrategy = new DeletionStrategy();

    public Xplain(T solver, boolean skipDuplicatedEntries) {
        super(solver, skipDuplicatedEntries);
    }

    public Xplain(T solver) {
        this(solver, true);
    }

    @Override
    public IConstr addAtMost(IVecInt literals, int degree) throws ContradictionException {
        throw new UnsupportedOperationException("Explanation requires Pseudo Boolean support. See XplainPB class instead.");
    }

    private IVecInt explanationKeys() throws TimeoutException {
        assert (!this.isSatisfiable(this.assump));
        Object solver = this.decorated();
        if (solver instanceof SolverDecorator) {
            solver = ((SolverDecorator)solver).decorated();
        }
        return this.xplainStrategy.explain((ISolver)solver, this.getConstrs(), this.assump);
    }

    public Collection<IConstr> explain() throws TimeoutException {
        IVecInt keys = this.explanationKeys();
        ArrayList<IConstr> explanation = new ArrayList<IConstr>(keys.size());
        IteratorInt it = keys.iterator();
        while (it.hasNext()) {
            explanation.add(this.getConstrs().get(it.next()));
        }
        return explanation;
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        this.assump = VecInt.EMPTY;
        return super.isSatisfiable();
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        this.assump = assumps;
        return super.isSatisfiable(assumps);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps, boolean global) throws TimeoutException {
        this.assump = assumps;
        return super.isSatisfiable(assumps, global);
    }

    @Override
    public boolean removeConstr(IConstr c) {
        if (this.getLastConstr() == c) {
            this.getLastClause().clear();
            this.setLastConstr(null);
        }
        return super.removeConstr(c);
    }

    @Override
    public boolean removeSubsumedConstr(IConstr c) {
        if (this.getLastConstr() == c) {
            this.getLastClause().clear();
            this.setLastConstr(null);
        }
        return super.removeSubsumedConstr(c);
    }
}

