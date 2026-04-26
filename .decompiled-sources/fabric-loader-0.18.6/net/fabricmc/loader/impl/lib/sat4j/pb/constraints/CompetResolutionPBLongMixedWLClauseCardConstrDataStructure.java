/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints;

import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.AbstractPBClauseCardConstrDataStructure;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.MaxLongWatchPBConstructor;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.MinCardConstructor;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.UnitBinaryWLClauseConstructor;

public class CompetResolutionPBLongMixedWLClauseCardConstrDataStructure
extends AbstractPBClauseCardConstrDataStructure {
    public CompetResolutionPBLongMixedWLClauseCardConstrDataStructure() {
        super(new UnitBinaryWLClauseConstructor(), new MinCardConstructor(), new MaxLongWatchPBConstructor());
    }
}

