/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.DataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Solver;

public interface PrimeImplicantStrategy {
    public int[] compute(Solver<? extends DataStructureFactory> var1);

    public int[] getPrimeImplicantAsArrayWithHoles();
}

