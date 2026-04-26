/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.pb.constraints.pb.WatchPbLong;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.MandatoryLiteralListener;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public final class MaxWatchPbLong
extends WatchPbLong {
    private long watchCumul = 0L;
    private final Map<Integer, Long> litToCoeffs;

    private MaxWatchPbLong(ILits voc, int[] lits, BigInteger[] coefs, BigInteger degree, BigInteger sumCoefs) {
        super(lits, coefs, degree, sumCoefs);
        this.voc = voc;
        this.activity = 0.0;
        this.watchCumul = 0L;
        if (coefs.length > 100) {
            this.litToCoeffs = new HashMap<Integer, Long>(this.coefs.length);
            for (int i = 0; i < this.coefs.length; ++i) {
                this.litToCoeffs.put(this.lits[i], this.coefs[i]);
            }
        } else {
            this.litToCoeffs = null;
        }
    }

    @Override
    protected void computeWatches() throws ContradictionException {
        int i;
        assert (this.watchCumul == 0L);
        for (i = 0; i < this.lits.length; ++i) {
            if (this.voc.isFalsified(this.lits[i])) {
                if (!this.learnt) continue;
                this.voc.undos(this.lits[i] ^ 1).push(this);
                this.voc.watch(this.lits[i] ^ 1, this);
                continue;
            }
            this.voc.watch(this.lits[i] ^ 1, this);
            this.watchCumul += this.coefs[i];
        }
        assert (this.watchCumul >= this.computeLeftSide());
        if (!this.learnt && this.watchCumul < this.degree) {
            for (i = 0; i < this.lits.length; ++i) {
                if (this.voc.isFalsified(this.lits[i])) continue;
                this.voc.watches(this.lits[i] ^ 1).remove(this);
            }
            throw new ContradictionException("non satisfiable constraint");
        }
    }

    @Override
    protected void computePropagation(UnitPropagationListener s) throws ContradictionException {
        for (int ind = 0; ind < this.coefs.length && this.watchCumul - this.coefs[ind] < this.degree; ++ind) {
            if (!this.voc.isUnassigned(this.lits[ind]) || s.enqueue(this.lits[ind], this)) continue;
            throw new ContradictionException("non satisfiable constraint");
        }
        assert (this.watchCumul >= this.computeLeftSide());
    }

    @Override
    public boolean propagate(UnitPropagationListener s, int p) {
        long coefP;
        this.voc.watch(p, this);
        assert (this.watchCumul >= this.computeLeftSide()) : "" + this.watchCumul + "/" + this.computeLeftSide() + ":" + this.learnt;
        if (this.litToCoeffs == null) {
            int indiceP = 0;
            while ((this.lits[indiceP] ^ 1) != p) {
                ++indiceP;
            }
            coefP = this.coefs[indiceP];
        } else {
            coefP = this.litToCoeffs.get(p ^ 1);
        }
        long newcumul = this.watchCumul - coefP;
        if (newcumul < this.degree) {
            assert (!this.isSatisfiable());
            return false;
        }
        this.voc.undos(p).push(this);
        this.watchCumul = newcumul;
        int trailPosition = this.voc.getTrailPosition(p);
        long limit = this.watchCumul - this.degree;
        for (int ind = 0; ind < this.coefs.length && limit < this.coefs[ind]; ++ind) {
            int lit = this.lits[ind];
            if (this.voc.isFalsified(lit) && this.voc.getTrailPosition(lit) > trailPosition) {
                assert (!this.isSatisfiable());
                return false;
            }
            if (!this.voc.isUnassigned(lit)) continue;
            boolean enqueued = s.enqueue(lit, this);
            assert (enqueued);
        }
        assert (this.learnt || this.watchCumul >= this.computeLeftSide());
        assert (this.watchCumul >= this.computeLeftSide());
        return true;
    }

    @Override
    public void remove(UnitPropagationListener upl) {
        for (int i = 0; i < this.lits.length; ++i) {
            if (this.voc.isFalsified(this.lits[i])) continue;
            this.voc.watches(this.lits[i] ^ 1).remove(this);
        }
        for (int ind = 0; ind < this.coefs.length && this.watchCumul - this.coefs[ind] < this.degree; ++ind) {
            if (this.voc.isUnassigned(this.lits[ind]) || this.voc.getReason(this.lits[ind]) != this) continue;
            upl.unset(this.lits[ind]);
        }
    }

    @Override
    public void undo(int p) {
        long coefP;
        if (this.litToCoeffs == null) {
            int indiceP;
            for (indiceP = 0; indiceP < this.lits.length && (this.lits[indiceP] ^ 1) != p; ++indiceP) {
            }
            coefP = indiceP == this.lits.length ? 0L : this.coefs[indiceP];
        } else {
            Long coefL = this.litToCoeffs.get(p ^ 1);
            coefP = coefL != null ? coefL : 0L;
        }
        this.watchCumul += coefP;
    }

    public static MaxWatchPbLong normalizedMaxWatchPbNew(UnitPropagationListener s, ILits voc, int[] lits, BigInteger[] coefs, BigInteger degree, BigInteger sumCoefs) throws ContradictionException {
        MaxWatchPbLong outclause = new MaxWatchPbLong(voc, lits, coefs, degree, sumCoefs);
        if (outclause.degree <= 0L) {
            return null;
        }
        outclause.computeWatches();
        outclause.computePropagation(s);
        return outclause;
    }

    @Override
    public boolean propagatePI(MandatoryLiteralListener l, int p) {
        long coefP;
        this.voc.watch(p, this);
        if (this.litToCoeffs == null) {
            int indiceP = 0;
            while ((this.lits[indiceP] ^ 1) != p) {
                ++indiceP;
            }
            coefP = this.coefs[indiceP];
        } else {
            coefP = this.litToCoeffs.get(p ^ 1);
        }
        long newcumul = this.watchCumul - coefP;
        this.voc.undos(p).push(this);
        this.watchCumul = newcumul;
        long limit = this.watchCumul - this.degree;
        for (int ind = 0; ind < this.coefs.length && limit < this.coefs[ind]; ++ind) {
            if (this.voc.isFalsified(this.lits[ind])) continue;
            l.isMandatory(this.lits[ind]);
        }
        return true;
    }

    @Override
    public int getAssertionLevel(IVecInt trail, int decisionLevel) {
        HashSet<Integer> litsSet = new HashSet<Integer>();
        int[] nArray = this.lits;
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            Integer i2 = nArray[i];
            litsSet.add(i2);
        }
        for (int i = 0; i < trail.size(); ++i) {
            if (!litsSet.contains(trail.get(i) ^ 1)) continue;
            return i;
        }
        return -1;
    }
}

