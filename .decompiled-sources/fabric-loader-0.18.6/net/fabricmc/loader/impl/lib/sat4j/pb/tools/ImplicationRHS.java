/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.tools;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.DependencyHelper;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.ImplicationAnd;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.ImplicationNamer;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVec;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;

public class ImplicationRHS<T, C> {
    private final IVecInt clause;
    private final DependencyHelper<T, C> helper;
    private final IVec<IConstr> toName = new Vec<IConstr>();

    public ImplicationRHS(DependencyHelper<T, C> helper, IVecInt clause) {
        this.clause = clause;
        this.helper = helper;
    }

    public ImplicationNamer<T, C> implies(T ... things) throws ContradictionException {
        for (T t : things) {
            this.clause.push(this.helper.getIntValue(t));
        }
        this.toName.push(this.helper.solver.addClause(this.clause));
        return new ImplicationNamer<T, C>(this.helper, this.toName);
    }

    public ImplicationAnd<T, C> impliesNot(T thing) throws ContradictionException {
        ImplicationAnd<T, C> and = new ImplicationAnd<T, C>(this.helper, this.clause);
        and.andNot(thing);
        return and;
    }
}

