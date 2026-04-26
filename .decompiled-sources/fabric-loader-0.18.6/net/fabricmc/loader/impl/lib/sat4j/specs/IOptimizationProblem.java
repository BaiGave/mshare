/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IProblem;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;

public interface IOptimizationProblem
extends IProblem {
    public boolean admitABetterSolution(IVecInt var1) throws TimeoutException;

    public boolean hasNoObjectiveFunction();

    public Number getObjectiveValue();

    public void discardCurrentSolution() throws ContradictionException;
}

