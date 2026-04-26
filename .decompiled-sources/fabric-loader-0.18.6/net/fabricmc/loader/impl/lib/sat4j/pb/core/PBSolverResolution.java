/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.IOrder;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.LearningStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.RestartStrategy;
import net.fabricmc.loader.impl.lib.sat4j.pb.core.PBDataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.pb.core.PBSolver;

public class PBSolverResolution
extends PBSolver {
    public PBSolverResolution(LearningStrategy<PBDataStructureFactory> learner, PBDataStructureFactory dsf, IOrder order, RestartStrategy restarter) {
        super(learner, dsf, order, restarter);
    }
}

