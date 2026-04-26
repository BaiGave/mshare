/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.card;

import java.io.Serializable;
import java.util.HashSet;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.Lits;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.UnitClauses;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Undoable;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.MandatoryLiteralListener;
import net.fabricmc.loader.impl.lib.sat4j.specs.Propagatable;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public class MinWatchCard
implements Serializable,
Undoable,
Constr,
Propagatable {
    protected int degree;
    protected final int[] lits;
    private boolean moreThan;
    protected int watchCumul;
    private final ILits voc;
    protected final int maxUnsatisfied;
    private int savedindex;

    public MinWatchCard(ILits voc, IVecInt ps, boolean moreThan, int degree) {
        this.savedindex = this.degree + 1;
        this.voc = voc;
        this.degree = degree;
        this.moreThan = moreThan;
        int[] index = new int[voc.nVars() * 2 + 2];
        for (int i = 0; i < ps.size(); ++i) {
            int p = ps.get(i);
            if (index[p ^ 1] == 0) {
                int n = p;
                index[n] = index[n] + 1;
                continue;
            }
            int n = p ^ 1;
            index[n] = index[n] - 1;
        }
        int ind = 0;
        while (ind < ps.size()) {
            if (index[ps.get(ind)] > 0) {
                int n = ps.get(ind);
                index[n] = index[n] - 1;
                ++ind;
                continue;
            }
            if ((ps.get(ind) & 1) != 0) {
                --this.degree;
            }
            ps.delete(ind);
        }
        this.lits = new int[ps.size()];
        ps.moveTo(this.lits);
        this.normalize();
        this.maxUnsatisfied = this.lits.length - this.degree;
    }

    @Override
    public void calcReason(int p, IVecInt outReason) {
        int c = p == -1 ? -1 : 0;
        for (int q : this.lits) {
            if (!this.voc.isFalsified(q)) continue;
            outReason.push(q ^ 1);
            if (++c < this.maxUnsatisfied) continue;
            return;
        }
    }

    @Override
    public double getActivity() {
        return 0.0;
    }

    @Override
    public void incActivity(double claInc) {
    }

    @Override
    public void setActivity(double d) {
    }

    @Override
    public boolean learnt() {
        return false;
    }

    protected static int linearisation(ILits voc, IVecInt ps) {
        int modif = 0;
        int i = 0;
        while (i < ps.size()) {
            if (voc.isUnassigned(ps.get(i))) {
                ++i;
                continue;
            }
            if (voc.isSatisfied(ps.get(i))) {
                --modif;
            }
            ps.set(i, ps.last());
            ps.pop();
        }
        assert (modif <= 0);
        return modif;
    }

    @Override
    public boolean locked() {
        return true;
    }

    public static Constr minWatchCardNew(UnitPropagationListener s, ILits voc, IVecInt ps, boolean moreThan, int degree) throws ContradictionException {
        int mydegree = degree + MinWatchCard.linearisation(voc, ps);
        if (ps.size() < mydegree) {
            throw new ContradictionException();
        }
        if (ps.size() == mydegree) {
            for (int i = 0; i < ps.size(); ++i) {
                if (s.enqueue(ps.get(i))) continue;
                throw new ContradictionException();
            }
            return new UnitClauses(ps);
        }
        MinWatchCard retour = new MinWatchCard(voc, ps, moreThan, mydegree);
        if (retour.degree <= 0) {
            return null;
        }
        retour.computeWatches();
        retour.computePropagation(s);
        return retour;
    }

    public final void normalize() {
        if (!this.moreThan) {
            this.degree = 0 - this.degree;
            for (int indLit = 0; indLit < this.lits.length; ++indLit) {
                this.lits[indLit] = this.lits[indLit] ^ 1;
                ++this.degree;
            }
            this.moreThan = true;
        }
    }

    @Override
    public boolean propagate(UnitPropagationListener s, int p) {
        int indSwap;
        this.savedindex = this.degree + 1;
        if (this.watchCumul == this.degree) {
            this.voc.watch(p, this);
            return false;
        }
        int indFalsified = 0;
        while ((this.lits[indFalsified] ^ 1) != p) {
            ++indFalsified;
        }
        assert (this.watchCumul > this.degree);
        for (indSwap = this.degree + 1; indSwap < this.lits.length && this.voc.isFalsified(this.lits[indSwap]); ++indSwap) {
        }
        if (indSwap == this.lits.length) {
            this.voc.watch(p, this);
            --this.watchCumul;
            assert (this.watchCumul == this.degree);
            this.voc.undos(p).push(this);
            for (int i = 0; i <= this.degree; ++i) {
                if (p == (this.lits[i] ^ 1) || s.enqueue(this.lits[i], this)) continue;
                return false;
            }
            return true;
        }
        int tmpInt = this.lits[indSwap];
        this.lits[indSwap] = this.lits[indFalsified];
        this.lits[indFalsified] = tmpInt;
        this.voc.watch(tmpInt ^ 1, this);
        return true;
    }

    @Override
    public void remove(UnitPropagationListener upl) {
        for (int i = 0; i < Math.min(this.degree + 1, this.lits.length); ++i) {
            this.voc.watches(this.lits[i] ^ 1).remove(this);
        }
    }

    @Override
    public void rescaleBy(double d) {
    }

    @Override
    public boolean simplify() {
        int count = 0;
        for (int i = 0; i < this.lits.length; ++i) {
            if (!this.voc.isSatisfied(this.lits[i]) || ++count != this.degree) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder stb = new StringBuilder();
        if (this.lits.length > 0) {
            stb.append(Lits.toStringX(this.lits[0]));
            stb.append("[");
            stb.append(this.voc.valueToString(this.lits[0]));
            stb.append("]");
            stb.append(" ");
            for (int i = 1; i < this.lits.length; ++i) {
                stb.append(Lits.toStringX(this.lits[i]));
                stb.append("[");
                stb.append(this.voc.valueToString(this.lits[i]));
                stb.append("]");
                stb.append(" ");
            }
            stb.append(">= ");
            stb.append(this.degree);
        }
        return stb.toString();
    }

    @Override
    public void undo(int p) {
        ++this.watchCumul;
    }

    @Override
    public void setLearnt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void register() {
        this.computeWatches();
    }

    @Override
    public int size() {
        return this.lits.length;
    }

    @Override
    public int get(int i) {
        return this.lits[i];
    }

    @Override
    public void assertConstraint(UnitPropagationListener s) {
        boolean ret = true;
        int[] nArray = this.lits;
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            Integer lit = nArray[i];
            if (!this.voc.isUnassigned(lit)) continue;
            ret &= s.enqueue(lit, this);
        }
        assert (ret);
    }

    protected void computeWatches() {
        int tmpInt;
        int indSwap = this.lits.length;
        for (int i = 0; i <= this.degree && i < indSwap; ++i) {
            while (this.voc.isFalsified(this.lits[i]) && --indSwap > i) {
                tmpInt = this.lits[i];
                this.lits[i] = this.lits[indSwap];
                this.lits[indSwap] = tmpInt;
            }
            if (this.voc.isFalsified(this.lits[i])) continue;
            ++this.watchCumul;
            this.voc.watch(this.lits[i] ^ 1, this);
        }
        if (this.watchCumul <= this.degree) {
            int free = 1;
            while (this.watchCumul <= this.degree && free > 0) {
                free = 0;
                int maxlevel = -1;
                int maxi = -1;
                for (int i = this.watchCumul; i < this.lits.length; ++i) {
                    if (!this.voc.isFalsified(this.lits[i])) continue;
                    ++free;
                    int level = this.voc.getLevel(this.lits[i]);
                    if (level <= maxlevel) continue;
                    maxi = i;
                    maxlevel = level;
                }
                if (free <= 0) continue;
                assert (maxi >= 0);
                this.voc.watch(this.lits[maxi] ^ 1, this);
                tmpInt = this.lits[maxi];
                this.lits[maxi] = this.lits[this.watchCumul];
                this.lits[this.watchCumul] = tmpInt;
                ++this.watchCumul;
                assert (--free >= 0);
            }
            assert (this.lits.length == 1 || this.watchCumul > 1);
        }
    }

    protected MinWatchCard computePropagation(UnitPropagationListener s) throws ContradictionException {
        if (this.watchCumul == this.degree) {
            for (int i = 0; i < this.lits.length; ++i) {
                if (s.enqueue(this.lits[i])) continue;
                throw new ContradictionException();
            }
            return null;
        }
        if (this.watchCumul < this.degree) {
            throw new ContradictionException();
        }
        return this;
    }

    public boolean equals(Object card) {
        if (card == null) {
            return false;
        }
        if (this.getClass() != card.getClass()) {
            return false;
        }
        try {
            MinWatchCard mcard = (MinWatchCard)card;
            if (mcard.degree != this.degree) {
                return false;
            }
            if (this.lits.length != mcard.lits.length) {
                return false;
            }
            for (int lit : this.lits) {
                boolean ok = false;
                for (int lit2 : mcard.lits) {
                    if (lit != lit2) continue;
                    ok = true;
                    break;
                }
                if (ok) continue;
                return false;
            }
            return true;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        long sum = 0L;
        for (int p : this.lits) {
            sum += (long)p;
        }
        return (int)(sum += (long)this.degree) / (this.lits.length + 1);
    }

    @Override
    public boolean canBePropagatedMultipleTimes() {
        return false;
    }

    @Override
    public Constr toConstraint() {
        return this;
    }

    @Override
    public boolean propagatePI(MandatoryLiteralListener l, int p) {
        int indSwap;
        int indFalsified = 0;
        while ((this.lits[indFalsified] ^ 1) != p) {
            ++indFalsified;
        }
        assert (this.watchCumul >= this.degree);
        for (indSwap = this.savedindex; indSwap < this.lits.length && this.voc.isFalsified(this.lits[indSwap]); ++indSwap) {
        }
        if (indSwap == this.lits.length) {
            this.voc.watch(p, this);
            for (int i = 0; i <= this.degree; ++i) {
                if (p == (this.lits[i] ^ 1)) continue;
                l.isMandatory(this.lits[i]);
            }
            return true;
        }
        this.savedindex = indSwap + 1;
        int tmpInt = this.lits[indSwap];
        this.lits[indSwap] = this.lits[indFalsified];
        this.lits[indFalsified] = tmpInt;
        this.voc.watch(tmpInt ^ 1, this);
        return true;
    }

    @Override
    public boolean canBeSatisfiedByCountingLiterals() {
        return true;
    }

    @Override
    public int requiredNumberOfSatisfiedLiterals() {
        return this.degree;
    }

    @Override
    public boolean isSatisfied() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public int getAssertionLevel(IVecInt trail, int decisionLevel) {
        int nUnsat = 0;
        HashSet<Integer> litsSet = new HashSet<Integer>();
        int[] nArray = this.lits;
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            Integer i2 = nArray[i];
            litsSet.add(i2);
        }
        for (int i = 0; i < trail.size(); ++i) {
            if (!litsSet.contains(trail.get(i) ^ 1) || ++nUnsat != this.maxUnsatisfied) continue;
            return i;
        }
        return -1;
    }
}

