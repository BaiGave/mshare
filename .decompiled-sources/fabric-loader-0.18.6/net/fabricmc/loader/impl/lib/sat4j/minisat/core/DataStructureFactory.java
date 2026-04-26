/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Learner;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface DataStructureFactory {
    public Constr createClause(IVecInt var1) throws ContradictionException;

    public Constr createUnregisteredClause(IVecInt var1);

    public void learnConstraint(Constr var1);

    public Constr createCardinalityConstraint(IVecInt var1, int var2) throws ContradictionException;

    public void setUnitPropagationListener(UnitPropagationListener var1);

    public void setLearner(Learner var1);

    public void reset();

    public ILits getVocabulary();
}

