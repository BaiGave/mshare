/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Undoable;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVec;
import net.fabricmc.loader.impl.lib.sat4j.specs.Propagatable;

public interface ILits {
    public int getFromPool(int var1);

    public boolean belongsToPool(int var1);

    public void resetPool();

    public void unassign(int var1);

    public void satisfies(int var1);

    public void forgets(int var1);

    public boolean isSatisfied(int var1);

    public boolean isFalsified(int var1);

    public boolean isUnassigned(int var1);

    public int nVars();

    public int realnVars();

    public int nextFreeVarId(boolean var1);

    public int getLevel(int var1);

    public void setLevel(int var1, int var2);

    public Constr getReason(int var1);

    public void setReason(int var1, Constr var2);

    public IVec<Undoable> undos(int var1);

    public void watch(int var1, Propagatable var2);

    public IVec<Propagatable> watches(int var1);

    public String valueToString(int var1);

    public void setTrailPosition(int var1, int var2);

    public int getTrailPosition(int var1);
}

