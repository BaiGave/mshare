/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import java.io.PrintWriter;
import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IProblem;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;

public interface ISolver
extends Serializable,
IProblem {
    public int nextFreeVarId(boolean var1);

    public IConstr addClause(IVecInt var1) throws ContradictionException;

    public IVecInt createBlockingClauseForCurrentModel();

    public boolean removeConstr(IConstr var1);

    public boolean removeSubsumedConstr(IConstr var1);

    public IConstr addAtMost(IVecInt var1, int var2) throws ContradictionException;

    public void setTimeout(int var1);

    public void expireTimeout();

    public void reset();

    @Deprecated
    public void printStat(PrintWriter var1, String var2);

    public boolean isVerbose();

    public String getLogPrefix();

    public IVecInt unsatExplanation();

    public int[] modelWithInternalVariables();
}

