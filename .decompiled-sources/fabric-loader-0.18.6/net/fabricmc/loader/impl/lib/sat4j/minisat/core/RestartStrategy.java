/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.SearchParams;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.SolverStats;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public interface RestartStrategy
extends Serializable,
ConflictTimer {
    public void init(SearchParams var1, SolverStats var2);

    public boolean shouldRestart();

    public void onRestart();

    public void onBackjumpToRootLevel();

    public void newLearnedClause(Constr var1, int var2);
}

