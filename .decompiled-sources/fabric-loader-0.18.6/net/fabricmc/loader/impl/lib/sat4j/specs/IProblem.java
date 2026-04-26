/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.RandomAccessModel;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;

public interface IProblem
extends RandomAccessModel {
    public int[] model();

    public int[] primeImplicant();

    public boolean primeImplicant(int var1);

    public boolean isSatisfiable() throws TimeoutException;

    public boolean isSatisfiable(IVecInt var1, boolean var2) throws TimeoutException;

    public boolean isSatisfiable(IVecInt var1) throws TimeoutException;

    public int nVars();
}

