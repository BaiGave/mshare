/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.orders;

import net.fabricmc.loader.impl.lib.sat4j.core.LiteralsUtils;
import net.fabricmc.loader.impl.lib.sat4j.minisat.orders.AbstractPhaserecordingSelectionStrategy;

public final class RSATPhaseSelectionStrategy
extends AbstractPhaserecordingSelectionStrategy {
    @Override
    public void assignLiteral(int p) {
        this.phase[LiteralsUtils.var((int)p)] = p;
    }

    public String toString() {
        return "lightweight component caching from RSAT";
    }

    @Override
    public void updateVar(int p) {
    }

    @Override
    public void updateVarAtDecisionLevel(int p) {
    }
}

