/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.orders;

import net.fabricmc.loader.impl.lib.sat4j.core.LiteralsUtils;
import net.fabricmc.loader.impl.lib.sat4j.minisat.orders.AbstractPhaserecordingSelectionStrategy;

public final class PhaseInLastLearnedClauseSelectionStrategy
extends AbstractPhaserecordingSelectionStrategy {
    @Override
    public void updateVar(int p) {
        this.phase[LiteralsUtils.var((int)p)] = p;
    }

    public String toString() {
        return "phase appearing in latest learned clause";
    }

    @Override
    public void assignLiteral(int p) {
    }

    @Override
    public void updateVarAtDecisionLevel(int q) {
    }
}

