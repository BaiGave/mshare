/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints;

import java.math.BigInteger;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.IPBConstructor;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.MaxWatchPb;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.MaxWatchPbLong;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.Propagatable;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public class MaxLongWatchPBConstructor
implements IPBConstructor {
    @Override
    public Constr constructPB(UnitPropagationListener solver, ILits voc, int[] theLits, BigInteger[] coefs, BigInteger degree, BigInteger sumCoefs) throws ContradictionException {
        Propagatable constr = sumCoefs.bitLength() < 64 ? MaxWatchPbLong.normalizedMaxWatchPbNew(solver, voc, theLits, coefs, degree, sumCoefs) : MaxWatchPb.normalizedMaxWatchPbNew(solver, voc, theLits, coefs, degree, sumCoefs);
        if (constr == null) {
            return Constr.TAUTOLOGY;
        }
        return constr;
    }
}

