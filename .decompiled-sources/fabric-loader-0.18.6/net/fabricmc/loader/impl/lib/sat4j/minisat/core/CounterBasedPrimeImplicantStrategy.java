/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.core.LiteralsUtils;
import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.DataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.PrimeImplicantStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Solver;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.IteratorInt;

public class CounterBasedPrimeImplicantStrategy
implements PrimeImplicantStrategy {
    private int[] prime;

    @Override
    public int[] compute(Solver<? extends DataStructureFactory> solver) {
        long begin = System.currentTimeMillis();
        IVecInt[] watched = new IVecInt[solver.voc.nVars() * 2 + 2];
        for (int d : solver.fullmodel) {
            watched[LiteralsUtils.toInternal((int)d)] = new VecInt();
        }
        int[] count = new int[solver.constrs.size()];
        for (int i = 0; i < solver.constrs.size(); ++i) {
            Constr constr = solver.constrs.get(i);
            if (!constr.canBeSatisfiedByCountingLiterals()) {
                throw new IllegalStateException("Algo2 does not work with constraints other than clauses and cardinalities" + constr.getClass());
            }
            count[i] = 0;
            for (int j = 0; j < constr.size(); ++j) {
                IVecInt watch = watched[constr.get(j)];
                if (watch == null) continue;
                watch.push(i);
            }
        }
        for (int d : solver.fullmodel) {
            IteratorInt it = watched[LiteralsUtils.toInternal(d)].iterator();
            while (it.hasNext()) {
                int n = it.next();
                count[n] = count[n] + 1;
            }
        }
        this.prime = new int[solver.voc.nVars() + 1];
        for (int i = 0; i < this.prime.length; ++i) {
            this.prime[i] = 0;
        }
        IteratorInt it = solver.implied.iterator();
        while (it.hasNext()) {
            int d;
            this.prime[Math.abs((int)d)] = d = it.next();
        }
        int removed = 0;
        int posremoved = 0;
        int propagated = 0;
        block7: for (int i = 0; i < solver.decisions.size(); ++i) {
            int d = solver.decisions.get(i);
            IteratorInt it2 = watched[LiteralsUtils.toInternal(d)].iterator();
            while (it2.hasNext()) {
                int constrNumber = it2.next();
                if (count[constrNumber] != solver.constrs.get(constrNumber).requiredNumberOfSatisfiedLiterals()) continue;
                this.prime[Math.abs((int)d)] = d;
                ++propagated;
                continue block7;
            }
            ++removed;
            if (d > 0 && d > solver.nVars()) {
                ++posremoved;
            }
            it2 = watched[LiteralsUtils.toInternal(d)].iterator();
            while (it2.hasNext()) {
                int n = it2.next();
                count[n] = count[n] - 1;
            }
        }
        int[] implicant = new int[this.prime.length - removed - 1];
        int index = 0;
        for (int i : this.prime) {
            if (i == 0) continue;
            implicant[index++] = i;
        }
        long end = System.currentTimeMillis();
        if (solver.isVerbose()) {
            System.out.printf("%s prime implicant computation statistics ALGO2%n", solver.getLogPrefix());
            System.out.printf("%s implied: %d, decision: %d, removed %d (+%d), propagated %d, time(ms):%d %n", solver.getLogPrefix(), solver.implied.size(), solver.decisions.size(), removed, posremoved, propagated, end - begin);
        }
        return implicant;
    }

    @Override
    public int[] getPrimeImplicantAsArrayWithHoles() {
        if (this.prime == null) {
            throw new UnsupportedOperationException("Call the compute method first!");
        }
        return this.prime;
    }
}

