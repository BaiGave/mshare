/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface IClauseConstructor {
    public Constr constructClause(UnitPropagationListener var1, ILits var2, IVecInt var3);

    public Constr constructLearntClause(ILits var1, IVecInt var2);
}

