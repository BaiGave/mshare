/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.core.LiteralsUtils;
import net.fabricmc.loader.impl.lib.sat4j.minisat.constraints.cnf.Lits;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.Propagatable;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public abstract class HTClause
implements Serializable,
Constr,
Propagatable {
    protected double activity;
    protected final int[] middleLits;
    protected final ILits voc;
    protected int head;
    protected int tail;

    public HTClause(IVecInt ps, ILits voc) {
        assert (ps.size() > 1);
        this.head = ps.get(0);
        this.tail = ps.last();
        int size = ps.size() - 2;
        assert (size > 0);
        this.middleLits = new int[size];
        System.arraycopy(ps.toArray(), 1, this.middleLits, 0, size);
        ps.clear();
        assert (ps.size() == 0);
        this.voc = voc;
        this.activity = 0.0;
    }

    @Override
    public void calcReason(int p, IVecInt outReason) {
        int[] mylits;
        if (this.voc.isFalsified(this.head)) {
            outReason.push(LiteralsUtils.neg(this.head));
        }
        for (int mylit : mylits = this.middleLits) {
            if (!this.voc.isFalsified(mylit)) continue;
            outReason.push(LiteralsUtils.neg(mylit));
        }
        if (this.voc.isFalsified(this.tail)) {
            outReason.push(LiteralsUtils.neg(this.tail));
        }
    }

    @Override
    public void remove(UnitPropagationListener upl) {
        this.voc.watches(LiteralsUtils.neg(this.head)).remove(this);
        this.voc.watches(LiteralsUtils.neg(this.tail)).remove(this);
    }

    @Override
    public boolean simplify() {
        if (this.voc.isSatisfied(this.head) || this.voc.isSatisfied(this.tail)) {
            return true;
        }
        for (int middleLit : this.middleLits) {
            if (!this.voc.isSatisfied(middleLit)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean propagate(UnitPropagationListener s, int p) {
        int temptail;
        if (this.head == LiteralsUtils.neg(p)) {
            int temphead;
            int[] mylits = this.middleLits;
            for (temphead = 0; temphead < mylits.length && this.voc.isFalsified(mylits[temphead]); ++temphead) {
            }
            assert (temphead <= mylits.length);
            if (temphead == mylits.length) {
                this.voc.watch(p, this);
                return s.enqueue(this.tail, this);
            }
            this.head = mylits[temphead];
            mylits[temphead] = LiteralsUtils.neg(p);
            this.voc.watch(LiteralsUtils.neg(this.head), this);
            return true;
        }
        assert (this.tail == LiteralsUtils.neg(p));
        int[] mylits = this.middleLits;
        for (temptail = mylits.length - 1; temptail >= 0 && this.voc.isFalsified(mylits[temptail]); --temptail) {
        }
        assert (-1 <= temptail);
        if (-1 == temptail) {
            this.voc.watch(p, this);
            return s.enqueue(this.head, this);
        }
        this.tail = mylits[temptail];
        mylits[temptail] = LiteralsUtils.neg(p);
        this.voc.watch(LiteralsUtils.neg(this.tail), this);
        return true;
    }

    @Override
    public boolean locked() {
        return this.voc.getReason(this.head) == this || this.voc.getReason(this.tail) == this;
    }

    @Override
    public double getActivity() {
        return this.activity;
    }

    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append(Lits.toString(this.head));
        stb.append("[");
        stb.append(this.voc.valueToString(this.head));
        stb.append("]");
        stb.append(" ");
        for (int middleLit : this.middleLits) {
            stb.append(Lits.toString(middleLit));
            stb.append("[");
            stb.append(this.voc.valueToString(middleLit));
            stb.append("]");
            stb.append(" ");
        }
        stb.append(Lits.toString(this.tail));
        stb.append("[");
        stb.append(this.voc.valueToString(this.tail));
        stb.append("]");
        return stb.toString();
    }

    @Override
    public int get(int i) {
        if (i == 0) {
            return this.head;
        }
        if (i == this.middleLits.length + 1) {
            return this.tail;
        }
        return this.middleLits[i - 1];
    }

    @Override
    public void rescaleBy(double d) {
        this.activity *= d;
    }

    @Override
    public int size() {
        return this.middleLits.length + 2;
    }

    @Override
    public void assertConstraint(UnitPropagationListener s) {
        assert (this.voc.isUnassigned(this.head));
        boolean ret = s.enqueue(this.head, this);
        assert (ret);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        try {
            HTClause wcl = (HTClause)obj;
            if (wcl.head != this.head || wcl.tail != this.tail) {
                return false;
            }
            if (this.middleLits.length != wcl.middleLits.length) {
                return false;
            }
            for (int lit : this.middleLits) {
                boolean ok = false;
                for (int lit2 : wcl.middleLits) {
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
        long sum = (long)this.head + (long)this.tail;
        for (int p : this.middleLits) {
            sum += (long)p;
        }
        return (int)sum / this.middleLits.length;
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
    public boolean canBeSatisfiedByCountingLiterals() {
        return true;
    }

    @Override
    public int requiredNumberOfSatisfiedLiterals() {
        return 1;
    }

    @Override
    public boolean isSatisfied() {
        if (this.voc.isSatisfied(this.head)) {
            return true;
        }
        if (this.voc.isSatisfied(this.tail)) {
            return true;
        }
        for (int p : this.middleLits) {
            if (!this.voc.isSatisfied(p)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getAssertionLevel(IVecInt trail, int decisionLevel) {
        for (int i = trail.size() - 1; i >= 0; --i) {
            if (LiteralsUtils.var(trail.get(i)) != LiteralsUtils.var(this.head)) continue;
            return i;
        }
        return -1;
    }
}

