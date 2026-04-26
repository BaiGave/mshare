/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolverService;
import net.fabricmc.loader.impl.lib.sat4j.specs.Lbool;
import net.fabricmc.loader.impl.lib.sat4j.specs.RandomAccessModel;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitClauseConsumer;

public interface SearchListener<S extends ISolverService>
extends Serializable,
UnitClauseConsumer {
    public void init(S var1);

    public void assuming(int var1);

    public void propagating(int var1);

    public void enqueueing(int var1, IConstr var2);

    public void backtracking(int var1);

    public void adding(int var1);

    public void learn(IConstr var1);

    public void delete(IConstr var1);

    public void conflictFound(IConstr var1, int var2, int var3);

    public void conflictFound(int var1);

    public void solutionFound(int[] var1, RandomAccessModel var2);

    public void beginLoop();

    public void start();

    public void end(Lbool var1);

    public void restarting();

    public void backjump(int var1);

    public void cleaning();
}

