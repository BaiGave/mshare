/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb;

import java.math.BigInteger;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.card.AtLeast;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.OriginalBinaryClausePB;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.OriginalHTClausePB;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.PBConstr;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.UnitClausesPB;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public final class AtLeastPB
extends AtLeast
implements PBConstr {
    private final BigInteger degree;
    private boolean learnt = false;

    private AtLeastPB(ILits voc, IVecInt ps, int degree) {
        super(voc, ps, degree);
        this.degree = BigInteger.valueOf(degree);
    }

    public static PBConstr atLeastNew(UnitPropagationListener s, ILits voc, IVecInt ps, int n) throws ContradictionException {
        int degree = AtLeastPB.niceParameters(s, voc, ps, n);
        if (degree == 0) {
            return new UnitClausesPB(ps);
        }
        if (degree == 1) {
            if (ps.size() == 2) {
                return OriginalBinaryClausePB.brandNewClause(s, voc, ps);
            }
            return OriginalHTClausePB.brandNewClause(s, voc, ps);
        }
        return AtLeastPB.atLeastNew(voc, ps, degree);
    }

    public static AtLeastPB atLeastNew(ILits voc, IVecInt ps, int n) {
        AtLeastPB atleast = new AtLeastPB(voc, ps, n);
        atleast.register();
        return atleast;
    }

    public ILits getVocabulary() {
        return this.voc;
    }

    @Override
    public boolean learnt() {
        return this.learnt;
    }

    @Override
    public void setLearnt() {
        this.learnt = true;
    }

    @Override
    public void assertConstraint(UnitPropagationListener s) {
        for (int i = 0; i < this.size(); ++i) {
            if (!this.getVocabulary().isUnassigned(this.get(i))) continue;
            boolean ret = s.enqueue(this.get(i), this);
            assert (ret);
        }
    }
}

