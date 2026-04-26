/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.DataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Solver;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public interface LearningStrategy<D extends DataStructureFactory>
extends Serializable {
    public void init();

    public void learns(Constr var1);

    public void setSolver(Solver<D> var1);
}

