/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVec;

public interface LearnedConstraintsDeletionStrategy
extends Serializable {
    public void init();

    public ConflictTimer getTimer();

    public void reduce(IVec<Constr> var1);

    public void onClauseLearning(Constr var1);

    public void onConflictAnalysis(Constr var1);

    public void onPropagation(Constr var1, int var2);
}

