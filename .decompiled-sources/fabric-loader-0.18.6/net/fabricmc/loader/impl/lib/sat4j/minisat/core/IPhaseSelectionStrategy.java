/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;

public interface IPhaseSelectionStrategy
extends Serializable {
    public void updateVar(int var1);

    public void init(int var1);

    public void init(int var1, int var2);

    public void assignLiteral(int var1);

    public int select(int var1);

    public void updateVarAtDecisionLevel(int var1);
}

