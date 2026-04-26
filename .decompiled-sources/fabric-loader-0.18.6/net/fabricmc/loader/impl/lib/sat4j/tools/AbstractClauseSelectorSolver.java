/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.tools;

import java.util.Collection;
import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolver;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.IteratorInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;
import net.fabricmc.loader.impl.lib.sat4j.tools.SolverDecorator;

public abstract class AbstractClauseSelectorSolver<T extends ISolver>
extends SolverDecorator<T> {
    private int lastCreatedVar;
    private boolean pooledVarId = false;
    private final SelectorState external = new SelectorState(){

        private IVecInt getNegatedSelectors() {
            VecInt assumps = new VecInt();
            for (int var : AbstractClauseSelectorSolver.this.getAddedVars()) {
                assumps.push(-var);
            }
            return assumps;
        }

        @Override
        public boolean isSatisfiable(IVecInt assumps, boolean global) throws TimeoutException {
            IVecInt all = this.getNegatedSelectors();
            assumps.copyTo(all);
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable(all, global);
        }

        @Override
        public boolean isSatisfiable() throws TimeoutException {
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable(this.getNegatedSelectors());
        }

        @Override
        public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
            IVecInt all = this.getNegatedSelectors();
            assumps.copyTo(all);
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable(all);
        }
    };
    private final SelectorState internal = new SelectorState(){

        @Override
        public boolean isSatisfiable() throws TimeoutException {
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable();
        }

        @Override
        public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable(assumps);
        }

        @Override
        public boolean isSatisfiable(IVecInt assumps, boolean global) throws TimeoutException {
            return AbstractClauseSelectorSolver.this.decorated().isSatisfiable(assumps, global);
        }
    };
    private SelectorState selectedState = this.external;

    public AbstractClauseSelectorSolver(T solver) {
        super(solver);
    }

    public abstract Collection<Integer> getAddedVars();

    protected int createNewVar(IVecInt literals) {
        IteratorInt it = literals.iterator();
        while (it.hasNext()) {
            if (Math.abs(it.next()) <= this.nextFreeVarId(false)) continue;
            throw new IllegalStateException("Please call newVar(int) before adding constraints!!!");
        }
        if (this.pooledVarId) {
            this.pooledVarId = false;
            return this.lastCreatedVar;
        }
        this.lastCreatedVar = this.nextFreeVarId(true);
        return this.lastCreatedVar;
    }

    protected void discardLastestVar() {
        this.pooledVarId = true;
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps, boolean global) throws TimeoutException {
        return this.selectedState.isSatisfiable(assumps, global);
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        return this.selectedState.isSatisfiable();
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        return this.selectedState.isSatisfiable(assumps);
    }

    private static interface SelectorState {
        public boolean isSatisfiable() throws TimeoutException;

        public boolean isSatisfiable(IVecInt var1) throws TimeoutException;

        public boolean isSatisfiable(IVecInt var1, boolean var2) throws TimeoutException;
    }
}

