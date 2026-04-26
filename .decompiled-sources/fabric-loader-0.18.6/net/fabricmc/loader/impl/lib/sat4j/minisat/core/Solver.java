/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import net.fabricmc.loader.impl.lib.sat4j.core.LiteralsUtils;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ActivityComparator;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ActivityLCDS;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.AgeLCDS;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimerAdapter;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ConflictTimerContainer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Counter;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.CounterBasedPrimeImplicantStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.DataStructureFactory;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Glucose2LCDS;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ICDCL;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ILits;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.IOrder;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ISimplifier;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.LBDConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.LearnedConstraintsDeletionStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.LearningStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.MemoryBasedConflictTimer;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Pair;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.PrimeImplicantStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.QuadraticPrimeImplicantStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.RestartStrategy;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.SearchParams;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.SizeLCDS;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.SolverStats;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Undoable;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.VoidTracing;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.WatcherBasedPrimeImplicantStrategy;
import net.fabricmc.loader.impl.lib.sat4j.specs.AssignmentOrigin;
import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.ILogAble;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolverService;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVec;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.IteratorInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.Lbool;
import net.fabricmc.loader.impl.lib.sat4j.specs.Propagatable;
import net.fabricmc.loader.impl.lib.sat4j.specs.SearchListener;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitClauseConsumer;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitClauseProvider;

public class Solver<D extends DataStructureFactory>
implements ICDCL<D>,
ISolverService {
    protected ILogAble out;
    protected final IVec<Constr> constrs = new Vec<Constr>();
    protected final IVec<Constr> learnts = new Vec<Constr>();
    private double claInc = 1.0;
    private double claDecay = 1.0;
    protected int qhead = 0;
    protected final IVecInt trail = new VecInt();
    protected final IVecInt trailLim = new VecInt();
    protected int rootLevel;
    private int[] model = null;
    protected ILits voc;
    private IOrder order;
    private final ActivityComparator comparator = new ActivityComparator();
    SolverStats stats = new SolverStats();
    private LearningStrategy<D> learner;
    protected volatile boolean undertimeout;
    private long timeout = Integer.MAX_VALUE;
    private boolean timeBasedTimeout = true;
    protected D dsfactory;
    private SearchParams params;
    private final IVecInt __dimacs_out = new VecInt();
    protected SearchListener slistener = new VoidTracing();
    private RestartStrategy restarter;
    private final Map<String, Counter> constrTypes = new HashMap<String, Counter>();
    private boolean isDBSimplificationAllowed = false;
    final IVecInt learnedLiterals = new VecInt();
    boolean verbose = false;
    private boolean keepHot = false;
    private String prefix = "c ";
    private int declaredMaxVarId = 0;
    private UnitClauseProvider unitClauseProvider = UnitClauseProvider.VOID;
    private UnitClauseConsumer unitClauseConsumer = UnitClauseConsumer.VOID;
    private final boolean classifyLiterals = System.getProperty("color") != null;
    private boolean[] mseen = new boolean[0];
    private final IVecInt mpreason = new VecInt();
    private final IVecInt moutLearnt = new VecInt();
    public static final ISimplifier NO_SIMPLIFICATION = new ISimplifier(){

        @Override
        public void simplify(IVecInt outLearnt) {
        }

        public String toString() {
            return "No reason simplification";
        }
    };
    public final ISimplifier SIMPLE_SIMPLIFICATION = new ISimplifier(){

        @Override
        public void simplify(IVecInt conflictToReduce) {
            Solver.this.simpleSimplification(conflictToReduce);
        }

        public String toString() {
            return "Simple reason simplification";
        }
    };
    public final ISimplifier EXPENSIVE_SIMPLIFICATION = new ISimplifier(){

        @Override
        public void simplify(IVecInt conflictToReduce) {
            Solver.this.expensiveSimplification(conflictToReduce);
        }

        public String toString() {
            return "Expensive reason simplification";
        }
    };
    public final ISimplifier EXPENSIVE_SIMPLIFICATION_WLONLY = new ISimplifier(){

        @Override
        public void simplify(IVecInt conflictToReduce) {
            Solver.this.expensiveSimplificationWLOnly(conflictToReduce);
        }

        public String toString() {
            return "Expensive reason simplification specific for WL data structure";
        }
    };
    private ISimplifier simplifier = NO_SIMPLIFICATION;
    private final IVecInt analyzetoclear = new VecInt();
    private final IVecInt analyzestack = new VecInt();
    final IVec<Propagatable> watched = new Vec<Propagatable>();
    private final Pair analysisResult = new Pair();
    private boolean[] userbooleanmodel;
    private IVecInt unsatExplanationInTermsOfAssumptions;
    protected final IVecInt implied = new VecInt();
    protected final IVecInt decisions = new VecInt();
    private AssignmentOrigin[] assignmentOrigins;
    int[] fullmodel;
    protected int[] prime;
    private double timebegin = 0.0;
    private boolean needToReduceDB;
    private ConflictTimerContainer conflictCount;
    private transient Timer timer;
    private final ConflictTimer memoryTimer = new MemoryBasedConflictTimer(this, 500);
    public final LearnedConstraintsDeletionStrategy activity_based_low_memory = new ActivityLCDS(this, this.memoryTimer);
    private final ConflictTimer lbdTimer = new LBDConflictTimer(this, 1000);
    public final LearnedConstraintsDeletionStrategy lbd_based = new Glucose2LCDS(this, this.lbdTimer);
    public final LearnedConstraintsDeletionStrategy age_based = new AgeLCDS(this, this.lbdTimer);
    public final LearnedConstraintsDeletionStrategy activity_based = new ActivityLCDS(this, this.lbdTimer);
    public final LearnedConstraintsDeletionStrategy size_based = new SizeLCDS(this, this.lbdTimer);
    private LearnedConstraintsDeletionStrategy learnedConstraintsDeletionStrategy = this.lbd_based;
    private boolean lastConflictMeansUnsat;
    protected Constr sharedConflict;
    private final Comparator<Integer> dimacsLevel = new Comparator<Integer>(){

        @Override
        public int compare(Integer i1, Integer i2) {
            return Solver.this.voc.getLevel(Math.abs(i2)) - Solver.this.voc.getLevel(Math.abs(i1));
        }
    };

    public IVecInt dimacs2internal(IVecInt in) {
        this.__dimacs_out.clear();
        this.__dimacs_out.ensure(in.size());
        for (int i = 0; i < in.size(); ++i) {
            int p = in.get(i);
            if (p == 0) {
                throw new IllegalArgumentException("0 is not a valid variable identifier");
            }
            this.__dimacs_out.unsafePush(this.voc.getFromPool(p));
        }
        return this.__dimacs_out;
    }

    public Solver(LearningStrategy<D> learner, D dsf, IOrder order, RestartStrategy restarter) {
        this(learner, dsf, new SearchParams(), order, restarter);
    }

    public Solver(LearningStrategy<D> learner, D dsf, SearchParams params, IOrder order, RestartStrategy restarter) {
        this(learner, dsf, params, order, restarter, ILogAble.CONSOLE);
    }

    public Solver(LearningStrategy<D> learner, D dsf, SearchParams params, IOrder order, RestartStrategy restarter, ILogAble logger) {
        this.order = order;
        this.params = params;
        this.restarter = restarter;
        this.out = logger;
        this.setDataStructureFactory(dsf);
        this.setLearningStrategy(learner);
    }

    public final void setDataStructureFactory(D dsf) {
        this.dsfactory = dsf;
        this.dsfactory.setUnitPropagationListener(this);
        this.dsfactory.setLearner(this);
        this.voc = dsf.getVocabulary();
        this.order.setLits(this.voc);
    }

    @Override
    public boolean isVerbose() {
        return this.verbose;
    }

    public void setLearningStrategy(LearningStrategy<D> strategy) {
        if (this.learner != null) {
            this.learner.setSolver(null);
        }
        this.learner = strategy;
        strategy.setSolver(this);
    }

    @Override
    public void setTimeout(int t) {
        this.timeout = (long)t * 1000L;
        this.timeBasedTimeout = true;
        this.undertimeout = true;
    }

    public void setRestartStrategy(RestartStrategy restarter) {
        this.restarter = restarter;
    }

    @Override
    public void expireTimeout() {
        this.undertimeout = false;
        if (this.timeBasedTimeout) {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        } else if (this.conflictCount != null) {
            this.conflictCount = null;
        }
    }

    protected int nAssigns() {
        return this.trail.size();
    }

    @Override
    public void learn(Constr c) {
        this.slistener.learn(c);
        this.learnts.push(c);
        c.setLearnt();
        c.register();
        this.stats.incLearnedclauses();
        switch (c.size()) {
            case 2: {
                this.stats.incLearnedbinaryclauses();
                break;
            }
            case 3: {
                this.stats.incLearnedternaryclauses();
                break;
            }
        }
    }

    public final int decisionLevel() {
        return this.trailLim.size();
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        IVecInt vlits = this.dimacs2internal(literals);
        return this.addConstr(this.dsfactory.createClause(vlits));
    }

    @Override
    public boolean removeConstr(IConstr co) {
        if (co == null) {
            throw new IllegalArgumentException("Reference to the constraint to remove needed!");
        }
        Constr c = (Constr)co;
        c.remove(this);
        this.constrs.removeFromLast(c);
        this.clearLearntClauses();
        String type = c.getClass().getName();
        this.constrTypes.get(type).dec();
        return true;
    }

    @Override
    public boolean removeSubsumedConstr(IConstr co) {
        if (co == null) {
            throw new IllegalArgumentException("Reference to the constraint to remove needed!");
        }
        if (this.constrs.last() != co) {
            throw new IllegalArgumentException("Can only remove latest added constraint!!!");
        }
        Constr c = (Constr)co;
        c.remove(this);
        this.constrs.pop();
        String type = c.getClass().getName();
        this.constrTypes.get(type).dec();
        return true;
    }

    @Override
    public IConstr addAtMost(IVecInt literals, int degree) throws ContradictionException {
        int n = literals.size();
        VecInt opliterals = new VecInt(n);
        IteratorInt iterator = literals.iterator();
        while (iterator.hasNext()) {
            opliterals.push(-iterator.next());
        }
        return this.addAtLeast(opliterals, n - degree);
    }

    public IConstr addAtLeast(IVecInt literals, int degree) throws ContradictionException {
        IVecInt vlits = this.dimacs2internal(literals);
        return this.addConstr(this.dsfactory.createCardinalityConstraint(vlits, degree));
    }

    public boolean simplifyDB() {
        IVec[] cs = new IVec[]{this.constrs, this.learnts};
        for (int type = 0; type < 2; ++type) {
            int j = 0;
            for (int i = 0; i < cs[type].size(); ++i) {
                if (((Constr)cs[type].get(i)).simplify()) {
                    ((Constr)cs[type].get(i)).remove(this);
                    continue;
                }
                cs[type].moveTo(j++, i);
            }
            cs[type].shrinkTo(j);
        }
        return true;
    }

    @Override
    public int[] model() {
        if (this.model == null) {
            throw new UnsupportedOperationException("Call the solve method first!!!");
        }
        int[] nmodel = new int[this.model.length];
        System.arraycopy(this.model, 0, nmodel, 0, this.model.length);
        return nmodel;
    }

    @Override
    public boolean enqueue(int p) {
        return this.enqueue(p, null);
    }

    @Override
    public boolean enqueue(int p, Constr from) {
        assert (p > 1);
        if (this.voc.isSatisfied(p)) {
            return true;
        }
        if (this.voc.isFalsified(p)) {
            return false;
        }
        this.slistener.enqueueing(LiteralsUtils.toDimacs(p), from);
        this.voc.satisfies(p);
        this.voc.setLevel(p, this.decisionLevel());
        this.voc.setTrailPosition(p, this.trail.size());
        this.voc.setReason(p, from);
        this.trail.push(p);
        if (from != null && from.learnt()) {
            this.learnedConstraintsDeletionStrategy.onPropagation(from, p);
        }
        return true;
    }

    public void analyze(Constr confl, Pair results) throws TimeoutException {
        assert (confl != null);
        boolean[] seen = this.mseen;
        IVecInt outLearnt = this.moutLearnt;
        IVecInt preason = this.mpreason;
        outLearnt.clear();
        assert (outLearnt.size() == 0);
        for (int i = 0; i < seen.length; ++i) {
            seen[i] = false;
        }
        int counter = 0;
        int p = -1;
        outLearnt.push(-1);
        int outBtlevel = 0;
        Constr prevConfl = null;
        do {
            preason.clear();
            assert (confl != null);
            if (prevConfl != confl || confl.canBePropagatedMultipleTimes()) {
                confl.calcReason(p, preason);
                this.learnedConstraintsDeletionStrategy.onConflictAnalysis(confl);
                for (int j = 0; j < preason.size(); ++j) {
                    int q = preason.get(j);
                    this.order.updateVar(q);
                    if (seen[q >> 1]) continue;
                    seen[q >> 1] = true;
                    if (this.voc.getLevel(q) == this.decisionLevel()) {
                        ++counter;
                        this.order.updateVarAtDecisionLevel(q);
                        continue;
                    }
                    if (this.voc.getLevel(q) <= 0) continue;
                    outLearnt.push(q ^ 1);
                    outBtlevel = Math.max(outBtlevel, this.voc.getLevel(q));
                }
            }
            prevConfl = confl;
            do {
                p = this.trail.last();
                confl = this.voc.getReason(p);
                this.undoOne();
            } while (!seen[p >> 1]);
        } while (--counter > 0);
        outLearnt.set(0, p ^ 1);
        this.simplifier.simplify(outLearnt);
        Constr c = this.dsfactory.createUnregisteredClause(outLearnt);
        this.learnedConstraintsDeletionStrategy.onClauseLearning(c);
        results.setReason(c);
        assert (outBtlevel > -1);
        results.setBacktrackLevel(outBtlevel);
    }

    public IVecInt analyzeFinalConflictInTermsOfAssumptions(Constr confl, IVecInt assumps, int conflictingLiteral) {
        if (assumps.size() == 0) {
            return null;
        }
        while (!this.trailLim.isEmpty() && this.trailLim.last() == this.trail.size()) {
            this.trailLim.pop();
        }
        boolean[] seen = this.mseen;
        IVecInt outLearnt = this.moutLearnt;
        IVecInt preason = this.mpreason;
        outLearnt.clear();
        if (this.trailLim.size() == 0) {
            return outLearnt;
        }
        assert (outLearnt.size() == 0);
        for (int i = 0; i < seen.length; ++i) {
            seen[i] = false;
        }
        if (confl == null) {
            seen[conflictingLiteral >> 1] = true;
        }
        int p = -1;
        while (confl == null && this.trail.size() > 0 && this.trailLim.size() > 0) {
            p = this.trail.last();
            confl = this.voc.getReason(p);
            this.undoOne();
            if (confl == null && p == (conflictingLiteral ^ 1)) {
                outLearnt.push(LiteralsUtils.toDimacs(p));
            }
            if (this.trail.size() > this.trailLim.last()) continue;
            this.trailLim.pop();
        }
        if (confl == null) {
            return outLearnt;
        }
        do {
            preason.clear();
            confl.calcReason(p, preason);
            for (int j = 0; j < preason.size(); ++j) {
                int q = preason.get(j);
                if (seen[q >> 1]) continue;
                seen[q >> 1] = true;
                if (this.voc.getReason(q) != null || this.voc.getLevel(q) <= 0) continue;
                assert (assumps.contains(LiteralsUtils.toDimacs(q)));
                outLearnt.push(LiteralsUtils.toDimacs(q));
            }
            do {
                p = this.trail.last();
                confl = this.voc.getReason(p);
                this.undoOne();
                if (this.decisionLevel() <= 0 || this.trail.size() > this.trailLim.last()) continue;
                this.trailLim.pop();
            } while (this.trail.size() > 0 && this.decisionLevel() > 0 && (!seen[p >> 1] || confl == null));
        } while (this.decisionLevel() > 0);
        return outLearnt;
    }

    public void setSimplifier(ISimplifier simp) {
        this.simplifier = simp;
    }

    private void simpleSimplification(IVecInt conflictToReduce) {
        int i;
        boolean[] seen = this.mseen;
        int j = 1;
        block0: for (i = 1; i < conflictToReduce.size(); ++i) {
            Constr r = this.voc.getReason(conflictToReduce.get(i));
            if (r == null || r.canBePropagatedMultipleTimes()) {
                conflictToReduce.moveTo(j++, i);
                continue;
            }
            for (int k = 0; k < r.size(); ++k) {
                int p = r.get(k);
                if (seen[p >> 1] || !this.voc.isFalsified(p) || this.voc.getLevel(p) == 0) continue;
                conflictToReduce.moveTo(j++, i);
                continue block0;
            }
        }
        conflictToReduce.shrink(i - j);
        this.stats.incReducedliterals(i - j);
    }

    private void expensiveSimplification(IVecInt conflictToReduce) {
        int i;
        this.analyzetoclear.clear();
        conflictToReduce.copyTo(this.analyzetoclear);
        int j = 1;
        for (i = 1; i < conflictToReduce.size(); ++i) {
            if (this.voc.getReason(conflictToReduce.get(i)) != null && this.analyzeRemovable(conflictToReduce.get(i))) continue;
            conflictToReduce.moveTo(j++, i);
        }
        conflictToReduce.shrink(i - j);
        this.stats.incReducedliterals(i - j);
    }

    private boolean analyzeRemovable(int p) {
        assert (this.voc.getReason(p) != null);
        ILits lvoc = this.voc;
        IVecInt lanalyzestack = this.analyzestack;
        IVecInt lanalyzetoclear = this.analyzetoclear;
        lanalyzestack.clear();
        lanalyzestack.push(p);
        boolean[] seen = this.mseen;
        int top = lanalyzetoclear.size();
        while (lanalyzestack.size() > 0) {
            int q = lanalyzestack.last();
            assert (lvoc.getReason(q) != null);
            Constr c = lvoc.getReason(q);
            lanalyzestack.pop();
            if (c.canBePropagatedMultipleTimes()) {
                for (int j = top; j < lanalyzetoclear.size(); ++j) {
                    seen[lanalyzetoclear.get((int)j) >> 1] = false;
                }
                lanalyzetoclear.shrink(lanalyzetoclear.size() - top);
                return false;
            }
            for (int i = 0; i < c.size(); ++i) {
                int l = c.get(i);
                if (seen[LiteralsUtils.var(l)] || !lvoc.isFalsified(l) || lvoc.getLevel(l) == 0) continue;
                if (lvoc.getReason(l) == null) {
                    for (int j = top; j < lanalyzetoclear.size(); ++j) {
                        seen[lanalyzetoclear.get((int)j) >> 1] = false;
                    }
                    lanalyzetoclear.shrink(lanalyzetoclear.size() - top);
                    return false;
                }
                seen[l >> 1] = true;
                lanalyzestack.push(l);
                lanalyzetoclear.push(l);
            }
        }
        return true;
    }

    private void expensiveSimplificationWLOnly(IVecInt conflictToReduce) {
        int i;
        this.analyzetoclear.clear();
        conflictToReduce.copyTo(this.analyzetoclear);
        int j = 1;
        for (i = 1; i < conflictToReduce.size(); ++i) {
            if (this.voc.getReason(conflictToReduce.get(i)) != null && this.analyzeRemovableWLOnly(conflictToReduce.get(i))) continue;
            conflictToReduce.moveTo(j++, i);
        }
        conflictToReduce.shrink(i - j);
        this.stats.incReducedliterals(i - j);
    }

    private boolean analyzeRemovableWLOnly(int p) {
        assert (this.voc.getReason(p) != null);
        this.analyzestack.clear();
        this.analyzestack.push(p);
        boolean[] seen = this.mseen;
        int top = this.analyzetoclear.size();
        while (this.analyzestack.size() > 0) {
            int q = this.analyzestack.last();
            assert (this.voc.getReason(q) != null);
            Constr c = this.voc.getReason(q);
            this.analyzestack.pop();
            for (int i = 1; i < c.size(); ++i) {
                int l = c.get(i);
                if (seen[LiteralsUtils.var(l)] || this.voc.getLevel(l) == 0) continue;
                if (this.voc.getReason(l) == null) {
                    for (int j = top; j < this.analyzetoclear.size(); ++j) {
                        seen[this.analyzetoclear.get((int)j) >> 1] = false;
                    }
                    this.analyzetoclear.shrink(this.analyzetoclear.size() - top);
                    return false;
                }
                seen[l >> 1] = true;
                this.analyzestack.push(l);
                this.analyzetoclear.push(l);
            }
        }
        return true;
    }

    protected void undoOne() {
        int p = this.trail.last();
        assert (p > 1);
        assert (this.voc.getLevel(p) >= 0);
        int x = p >> 1;
        this.voc.unassign(p);
        this.voc.setReason(p, null);
        this.voc.setLevel(p, -1);
        this.voc.setTrailPosition(p, -1);
        this.order.undo(x);
        this.trail.pop();
        IVec<Undoable> undos = this.voc.undos(p);
        assert (undos != null);
        for (int size = undos.size(); size > 0; --size) {
            undos.last().undo(p);
            undos.pop();
        }
    }

    public void claBumpActivity(Constr confl) {
        confl.incActivity(this.claInc);
        if (confl.getActivity() > 1.0E20) {
            this.claRescalActivity();
        }
    }

    @Override
    public void varBumpActivity(int p) {
        this.order.updateVar(p);
    }

    private void claRescalActivity() {
        for (int i = 0; i < this.learnts.size(); ++i) {
            this.learnts.get(i).rescaleBy(1.0E-20);
        }
        this.claInc *= 1.0E-20;
    }

    public final Constr propagate() {
        IVecInt ltrail = this.trail;
        SolverStats lstats = this.stats;
        IOrder lorder = this.order;
        SearchListener lslistener = this.slistener;
        while (this.qhead < ltrail.size()) {
            lstats.incPropagations();
            int p = ltrail.get(this.qhead++);
            lslistener.propagating(LiteralsUtils.toDimacs(p));
            lorder.assignLiteral(p);
            Constr confl = this.reduceClausesContainingTheNegationOf(p);
            if (confl == null) continue;
            return confl;
        }
        return null;
    }

    private Constr reduceClausesContainingTheNegationOf(int p) {
        assert (p > 1);
        IVec<Propagatable> lwatched = this.watched;
        lwatched.clear();
        this.voc.watches(p).moveTo(lwatched);
        int size = lwatched.size();
        for (int i = 0; i < size; ++i) {
            this.stats.incInspects();
            if (lwatched.get(i).propagate(this, p)) continue;
            int sizew = lwatched.size();
            for (int j = i + 1; j < sizew; ++j) {
                this.voc.watch(p, lwatched.get(j));
            }
            this.qhead = this.trail.size();
            return lwatched.get(i).toConstraint();
        }
        return null;
    }

    void record(Constr constr) {
        constr.assertConstraint(this);
        int p = LiteralsUtils.toDimacs(constr.get(0));
        this.slistener.adding(p);
        if (constr.size() == 1) {
            this.stats.incLearnedliterals();
            this.slistener.learnUnit(p);
            this.unitClauseConsumer.learnUnit(p);
        } else {
            this.learner.learns(constr);
        }
    }

    public boolean assume(int p) {
        assert (!this.trailLim.contains(this.trail.size()));
        this.trailLim.push(this.trail.size());
        return this.enqueue(p);
    }

    void cancel() {
        int decisionvar = this.trail.unsafeGet(this.trailLim.last());
        this.slistener.backtracking(LiteralsUtils.toDimacs(decisionvar));
        for (int c = this.trail.size() - this.trailLim.last(); c > 0; --c) {
            this.undoOne();
        }
        this.trailLim.pop();
        this.qhead = this.trail.size();
    }

    private void cancelLearntLiterals(int learnedLiteralsLimit) {
        this.learnedLiterals.clear();
        while (this.trail.size() > learnedLiteralsLimit) {
            this.learnedLiterals.push(this.trail.last());
            this.undoOne();
        }
    }

    protected void cancelUntil(int level) {
        while (this.decisionLevel() > level) {
            this.cancel();
        }
    }

    protected void cancelUntilTrailLevel(int level) {
        while (!this.trail.isEmpty() && this.trail.size() > level) {
            this.undoOne();
            if (this.trailLim.isEmpty() || this.trailLim.last() != this.trail.size()) continue;
            this.trailLim.pop();
            this.decisions.pop();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private Lbool search(IVecInt assumps) {
        assert (this.rootLevel == this.decisionLevel());
        this.stats.incStarts();
        this.order.setVarDecay(1.0 / this.params.getVarDecay());
        this.claDecay = 1.0 / this.params.getClaDecay();
        do {
            Constr confl;
            block24: {
                boolean allsat;
                block27: {
                    block25: {
                        int p;
                        block26: {
                            this.slistener.beginLoop();
                            confl = this.propagate();
                            assert (this.trail.size() == this.qhead);
                            if (confl != null) break block24;
                            if (this.decisionLevel() == 0 && this.isDBSimplificationAllowed) {
                                this.stats.incRootSimplifications();
                                boolean ret = this.simplifyDB();
                                assert (ret);
                            }
                            assert (this.nAssigns() <= this.voc.realnVars());
                            if (this.nAssigns() == this.voc.realnVars()) {
                                this.modelFound();
                                this.slistener.solutionFound(this.fullmodel != null ? this.fullmodel : this.model, this);
                                if (this.sharedConflict == null) {
                                    this.cancelUntil(this.rootLevel);
                                    return Lbool.TRUE;
                                }
                                if (this.decisionLevel() == this.rootLevel) {
                                    confl = this.sharedConflict;
                                    this.sharedConflict = null;
                                    break block24;
                                } else {
                                    int level = this.sharedConflict.getAssertionLevel(this.trail, this.decisionLevel());
                                    this.cancelUntilTrailLevel(level);
                                    this.qhead = this.trail.size();
                                    this.sharedConflict.assertConstraint(this);
                                    this.sharedConflict = null;
                                    continue;
                                }
                            }
                            if (this.restarter.shouldRestart()) {
                                this.cancelUntil(this.rootLevel);
                                return Lbool.UNDEFINED;
                            }
                            if (this.needToReduceDB) {
                                this.reduceDB();
                                this.needToReduceDB = false;
                            }
                            if (this.sharedConflict != null) break block25;
                            this.stats.incDecisions();
                            p = this.order.select();
                            if (p != -1) break block26;
                            allsat = true;
                            break block27;
                        }
                        assert (p > 1);
                        this.slistener.assuming(LiteralsUtils.toDimacs(p));
                        boolean ret = this.assume(p);
                        assert (ret);
                        break block24;
                    }
                    confl = this.sharedConflict;
                    this.sharedConflict = null;
                    break block24;
                }
                for (int i = 0; i < this.constrs.size(); ++i) {
                    if (this.constrs.get(i).isSatisfied()) continue;
                    allsat = false;
                    break;
                }
                if (allsat) {
                    this.modelFound();
                    this.slistener.solutionFound(this.fullmodel != null ? this.fullmodel : this.model, this);
                    return Lbool.TRUE;
                }
                confl = this.preventTheSameDecisionsToBeMade();
                this.lastConflictMeansUnsat = false;
            }
            if (confl == null) continue;
            this.stats.incConflicts();
            this.slistener.conflictFound(confl, this.decisionLevel(), this.trail.size());
            this.conflictCount.newConflict();
            if (this.decisionLevel() == this.rootLevel) {
                if (this.lastConflictMeansUnsat) {
                    this.unsatExplanationInTermsOfAssumptions = this.analyzeFinalConflictInTermsOfAssumptions(confl, assumps, -1);
                    return Lbool.FALSE;
                }
                return Lbool.UNDEFINED;
            }
            int conflictTrailLevel = this.trail.size();
            try {
                this.analyze(confl, this.analysisResult);
            }
            catch (TimeoutException e) {
                return Lbool.UNDEFINED;
            }
            assert (this.analysisResult.getBacktrackLevel() < this.decisionLevel());
            int backjumpLevel = Math.max(this.analysisResult.getBacktrackLevel(), this.rootLevel);
            this.slistener.backjump(backjumpLevel);
            this.cancelUntil(backjumpLevel);
            if (backjumpLevel == this.rootLevel) {
                this.restarter.onBackjumpToRootLevel();
            }
            assert (this.decisionLevel() >= this.rootLevel && this.decisionLevel() >= this.analysisResult.getBacktrackLevel());
            if (this.analysisResult.getReason() == null) {
                return Lbool.FALSE;
            }
            this.record(this.analysisResult.getReason());
            this.restarter.newLearnedClause(this.analysisResult.getReason(), conflictTrailLevel);
            this.analysisResult.setReason(null);
            this.decayActivities();
        } while (this.undertimeout);
        return Lbool.UNDEFINED;
    }

    private Constr preventTheSameDecisionsToBeMade() {
        VecInt clause = new VecInt(this.nVars());
        for (int i = this.trail.size() - 1; i >= 0; --i) {
            int p = this.trail.get(i);
            if (this.voc.getReason(p) != null) continue;
            clause.push(p ^ 1);
        }
        return this.dsfactory.createUnregisteredClause(clause);
    }

    protected void analyzeAtRootLevel(Constr conflict) {
    }

    void modelFound() {
        int p;
        int i;
        Constr reason;
        this.decisions.clear();
        VecInt tempmodel = new VecInt(this.nVars());
        this.assignmentOrigins = new AssignmentOrigin[this.realNumberOfVariables()];
        this.userbooleanmodel = new boolean[this.realNumberOfVariables()];
        this.fullmodel = null;
        AssignmentOrigin origin = AssignmentOrigin.UNASSIGNED;
        if (this.classifyLiterals) {
            StringBuffer stb = new StringBuffer(this.getLogPrefix());
            for (int i2 = 0; i2 < this.trailLim.size(); ++i2) {
                int q = this.trail.get(this.trailLim.get(i2));
                stb.append(LiteralsUtils.toDimacs(q));
                this.voc.unassign(q);
                this.voc.satisfies(q ^ 1);
                reason = this.reduceClausesContainingTheNegationOf(q ^ 1);
                if (reason != null) {
                    origin = reason.learnt() ? AssignmentOrigin.DECIDED_PROPAGATED_LEARNED : AssignmentOrigin.DECIDED_PROPAGATED;
                    TreeSet<Integer> levels = new TreeSet<Integer>();
                    for (int j = 0; j < reason.size(); ++j) {
                        int r = reason.get(j);
                        if (r == q) continue;
                        levels.add(this.voc.getLevel(r));
                    }
                    stb.append(":");
                    String str = levels.toString().replaceAll(" ", "");
                    stb.append(str.substring(1, str.length() - 1));
                    if (this.voc.getLevel(q) == ((Integer)levels.last()).intValue()) {
                        origin = AssignmentOrigin.DECIDED_CYCLE;
                    }
                } else {
                    origin = AssignmentOrigin.DECIDED;
                }
                this.voc.unassign(q);
                this.voc.satisfies(q);
                stb.append(" ");
                this.assignmentOrigins[(q >> 1) - 1] = origin;
            }
            System.out.println(stb);
        }
        for (i = 1; i <= this.nVars(); ++i) {
            if (!this.voc.belongsToPool(i) || this.voc.isUnassigned(p = this.voc.getFromPool(i))) continue;
            tempmodel.push(this.voc.isSatisfied(p) ? i : -i);
            this.userbooleanmodel[i - 1] = this.voc.isSatisfied(p);
            reason = this.voc.getReason(p);
            if (reason == null && this.voc.getLevel(p) > 0 || reason != null && reason.learnt()) {
                this.decisions.push(tempmodel.last());
                if (reason == null) continue;
                this.assignmentOrigins[i - 1] = AssignmentOrigin.PROPAGATED_LEARNED;
                continue;
            }
            this.implied.push(tempmodel.last());
            this.assignmentOrigins[i - 1] = AssignmentOrigin.PROPAGATED_ORIGINAL;
        }
        this.model = new int[tempmodel.size()];
        tempmodel.copyTo(this.model);
        if (this.realNumberOfVariables() > this.nVars()) {
            for (i = this.nVars() + 1; i <= this.realNumberOfVariables(); ++i) {
                if (this.voc.belongsToPool(i)) {
                    p = this.voc.getFromPool(i);
                    if (this.voc.isUnassigned(p)) continue;
                    tempmodel.push(this.voc.isSatisfied(p) ? i : -i);
                    this.userbooleanmodel[i - 1] = this.voc.isSatisfied(p);
                    if (this.voc.getReason(p) == null) {
                        this.decisions.push(tempmodel.last());
                        continue;
                    }
                    this.implied.push(tempmodel.last());
                    if (this.voc.getReason(p).learnt()) {
                        this.assignmentOrigins[i - 1] = AssignmentOrigin.PROPAGATED_LEARNED;
                        continue;
                    }
                    this.assignmentOrigins[i - 1] = AssignmentOrigin.PROPAGATED_ORIGINAL;
                    continue;
                }
                this.assignmentOrigins[i - 1] = AssignmentOrigin.UNASSIGNED;
            }
            this.fullmodel = new int[tempmodel.size()];
            tempmodel.moveTo(this.fullmodel);
        } else {
            this.fullmodel = this.model;
        }
    }

    Constr forget(int var) {
        boolean satisfied = this.voc.isSatisfied(LiteralsUtils.toInternal(var));
        this.voc.forgets(var);
        Constr confl = satisfied ? this.reduceClausesContainingTheNegationOf(LiteralsUtils.toInternal(-var)) : this.reduceClausesContainingTheNegationOf(LiteralsUtils.toInternal(var));
        return confl;
    }

    @Override
    public int[] primeImplicant() {
        String primeApproach = System.getProperty("prime");
        PrimeImplicantStrategy strategy = "OLD".equals(primeApproach) ? new QuadraticPrimeImplicantStrategy() : ("ALGO2".equals(primeApproach) ? new CounterBasedPrimeImplicantStrategy() : new WatcherBasedPrimeImplicantStrategy());
        int[] implicant = strategy.compute(this);
        this.prime = strategy.getPrimeImplicantAsArrayWithHoles();
        return implicant;
    }

    @Override
    public boolean primeImplicant(int p) {
        if (p == 0 || Math.abs(p) > this.realNumberOfVariables()) {
            throw new IllegalArgumentException("Use a valid Dimacs var id as argument!");
        }
        if (this.prime == null) {
            throw new UnsupportedOperationException("Call the primeImplicant method first!!!");
        }
        return this.prime[Math.abs(p)] == p;
    }

    @Override
    public boolean model(int var) {
        if (var <= 0 || var > this.realNumberOfVariables()) {
            throw new IllegalArgumentException("Use a valid Dimacs var id as argument!");
        }
        if (this.userbooleanmodel == null) {
            throw new UnsupportedOperationException("Call the solve method first!!!");
        }
        return this.userbooleanmodel[var - 1];
    }

    public void clearLearntClauses() {
        Iterator<Constr> iterator = this.learnts.iterator();
        while (iterator.hasNext()) {
            iterator.next().remove(this);
        }
        this.learnts.clear();
        this.learnedLiterals.clear();
    }

    protected final void reduceDB() {
        this.stats.incReduceddb();
        this.slistener.cleaning();
        this.learnedConstraintsDeletionStrategy.reduce(this.learnts);
    }

    protected ActivityComparator getActivityComparator() {
        return this.comparator;
    }

    protected void decayActivities() {
        this.order.varDecayActivity();
        this.claDecayActivity();
    }

    private void claDecayActivity() {
        this.claInc *= this.claDecay;
    }

    @Override
    public boolean isSatisfiable() throws TimeoutException {
        return this.isSatisfiable(VecInt.EMPTY);
    }

    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        return this.isSatisfiable(assumps, false);
    }

    public void setLearnedConstraintsDeletionStrategy(LearnedConstraintsDeletionStrategy lcds) {
        if (this.conflictCount != null) {
            this.conflictCount.add(lcds.getTimer());
            assert (this.learnedConstraintsDeletionStrategy != null);
            this.conflictCount.remove(this.learnedConstraintsDeletionStrategy.getTimer());
        }
        this.learnedConstraintsDeletionStrategy = lcds;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isSatisfiable(IVecInt assumps, boolean global) throws TimeoutException {
        Lbool status = Lbool.UNDEFINED;
        boolean alreadylaunched = this.conflictCount != null;
        int howmany = this.voc.nVars();
        if (this.mseen.length <= howmany) {
            this.mseen = new boolean[howmany + 1];
        }
        this.trail.ensure(howmany);
        this.trailLim.ensure(howmany);
        this.learnedLiterals.ensure(howmany);
        this.decisions.clear();
        this.implied.clear();
        this.slistener.init(this);
        this.slistener.start();
        this.model = null;
        this.fullmodel = null;
        this.userbooleanmodel = null;
        this.prime = null;
        this.unsatExplanationInTermsOfAssumptions = null;
        VecInt localAssumps = new VecInt(assumps.size());
        IteratorInt iterator = assumps.iterator();
        while (iterator.hasNext()) {
            int assump = iterator.next();
            localAssumps.push(this.voc.getFromPool(assump));
        }
        if (!alreadylaunched || !this.keepHot) {
            this.order.init();
        }
        this.learnedConstraintsDeletionStrategy.init();
        int learnedLiteralsLimit = this.trail.size();
        this.qhead = 0;
        for (int i = learnedLiteralsLimit - 1; i >= 0; --i) {
            int p = this.trail.get(i);
            IVec<Undoable> undos = this.voc.undos(p);
            assert (undos != null);
            for (int size = undos.size(); size > 0; --size) {
                undos.last().undo(p);
                undos.pop();
            }
        }
        IteratorInt iterator2 = this.learnedLiterals.iterator();
        while (iterator2.hasNext()) {
            this.enqueue(iterator2.next());
        }
        Constr confl = this.propagate();
        if (confl != null) {
            this.analyzeAtRootLevel(confl);
            this.slistener.conflictFound(confl, 0, 0);
            this.slistener.end(Lbool.FALSE);
            this.cancelUntil(0);
            this.cancelLearntLiterals(learnedLiteralsLimit);
            return false;
        }
        IteratorInt iterator3 = localAssumps.iterator();
        while (iterator3.hasNext()) {
            int p = iterator3.next();
            if ((this.voc.isSatisfied(p) || this.assume(p)) && (confl = this.propagate()) == null) continue;
            if (confl == null) {
                this.slistener.conflictFound(p);
                this.unsatExplanationInTermsOfAssumptions = this.analyzeFinalConflictInTermsOfAssumptions(null, assumps, p);
                this.unsatExplanationInTermsOfAssumptions.push(LiteralsUtils.toDimacs(p));
            } else {
                this.slistener.conflictFound(confl, this.decisionLevel(), this.trail.size());
                this.unsatExplanationInTermsOfAssumptions = this.analyzeFinalConflictInTermsOfAssumptions(confl, assumps, -1);
            }
            this.slistener.end(Lbool.FALSE);
            this.cancelUntil(0);
            this.cancelLearntLiterals(learnedLiteralsLimit);
            return false;
        }
        this.rootLevel = this.decisionLevel();
        this.learner.init();
        if (!alreadylaunched) {
            this.conflictCount = new ConflictTimerContainer();
            this.conflictCount.add(this.restarter);
            this.conflictCount.add(this.learnedConstraintsDeletionStrategy.getTimer());
        }
        boolean firstTimeGlobal = false;
        if (this.timeBasedTimeout) {
            if (!global || this.timer == null) {
                firstTimeGlobal = true;
                this.undertimeout = true;
                TimerTask stopMe = new TimerTask(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void run() {
                        Solver.this.undertimeout = false;
                        Solver solver = Solver.this;
                        synchronized (solver) {
                            if (Solver.this.timer != null) {
                                Solver.this.timer.cancel();
                                Solver.this.timer = null;
                            }
                        }
                    }
                };
                this.timer = new Timer(true);
                this.timer.schedule(stopMe, this.timeout);
            }
        } else if (!global || !alreadylaunched) {
            firstTimeGlobal = true;
            this.undertimeout = true;
            ConflictTimerAdapter conflictTimeout = new ConflictTimerAdapter(this, (int)this.timeout){

                @Override
                public void run() {
                    this.getSolver().expireTimeout();
                }
            };
            this.conflictCount.add(conflictTimeout);
        }
        if (!global || firstTimeGlobal) {
            this.restarter.init(this.params, this.stats);
            this.timebegin = System.currentTimeMillis();
        }
        this.needToReduceDB = false;
        this.lastConflictMeansUnsat = true;
        while (status == Lbool.UNDEFINED && this.undertimeout && this.lastConflictMeansUnsat) {
            int before = this.trail.size();
            this.unitClauseProvider.provideUnitClauses(this);
            this.stats.incImportedUnits(this.trail.size() - before);
            status = this.search(assumps);
            if (status != Lbool.UNDEFINED) continue;
            this.restarter.onRestart();
            this.slistener.restarting();
        }
        this.cancelUntil(0);
        this.cancelLearntLiterals(learnedLiteralsLimit);
        if (!global && this.timeBasedTimeout) {
            Solver before = this;
            synchronized (before) {
                if (this.timer != null) {
                    this.timer.cancel();
                    this.timer = null;
                }
            }
        }
        this.slistener.end(status);
        if (!this.undertimeout) {
            String message = " Timeout (" + this.timeout + (this.timeBasedTimeout ? "ms" : " conflicts") + ") exceeded";
            throw new TimeoutException(message);
        }
        if (status == Lbool.UNDEFINED && !this.lastConflictMeansUnsat) {
            throw new TimeoutException("Cannot decide the satisfiability");
        }
        return this.model != null;
    }

    public void printLearntClausesInfos(PrintWriter out, String prefix) {
        if (this.learnts.isEmpty()) {
            return;
        }
        HashMap<String, Counter> learntTypes = new HashMap<String, Counter>();
        Iterator<Constr> it = this.learnts.iterator();
        while (it.hasNext()) {
            String type = it.next().getClass().getName();
            Counter count = (Counter)learntTypes.get(type);
            if (count == null) {
                learntTypes.put(type, new Counter());
                continue;
            }
            count.inc();
        }
        for (Map.Entry entry : learntTypes.entrySet()) {
            out.println(prefix + "learnt constraints type " + (String)entry.getKey() + "\t: " + entry.getValue());
        }
    }

    protected void initStats(SolverStats myStats) {
        this.stats = myStats;
    }

    public IOrder getOrder() {
        return this.order;
    }

    @Override
    public void reset() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.trail.clear();
        this.trailLim.clear();
        this.qhead = 0;
        Iterator<Constr> iterator = this.constrs.iterator();
        while (iterator.hasNext()) {
            iterator.next().remove(this);
        }
        this.constrs.clear();
        this.clearLearntClauses();
        this.voc.resetPool();
        this.dsfactory.reset();
        this.stats.reset();
        this.constrTypes.clear();
        this.undertimeout = true;
        this.declaredMaxVarId = 0;
    }

    @Override
    public int nVars() {
        if (this.declaredMaxVarId == 0) {
            return this.voc.nVars();
        }
        return this.declaredMaxVarId;
    }

    public IConstr addConstr(Constr constr) {
        if (constr == null) {
            Counter count = this.constrTypes.get("ignored satisfied constraints");
            if (count == null) {
                this.constrTypes.put("ignored satisfied constraints", new Counter());
            } else {
                count.inc();
            }
        } else {
            this.constrs.push(constr);
            String type = constr.getClass().getName();
            Counter count = this.constrTypes.get(type);
            if (count == null) {
                this.constrTypes.put(type, new Counter());
            } else {
                count.inc();
            }
        }
        return constr;
    }

    public DataStructureFactory getDSFactory() {
        return this.dsfactory;
    }

    @Override
    public void printStat(PrintWriter out, String prefix) {
        this.stats.printStat(out, prefix);
        double cputime = ((double)System.currentTimeMillis() - this.timebegin) / 1000.0;
        out.println(prefix + "speed (assignments/second)\t: " + (double)this.stats.getPropagations() / cputime);
        this.order.printStat(out, prefix);
        this.printLearntClausesInfos(out, prefix);
    }

    public String toString(String prefix) {
        StringBuilder stb = new StringBuilder();
        Object[] objs = new Object[]{this.dsfactory, this.learner, this.params, this.order, this.simplifier, this.restarter, this.learnedConstraintsDeletionStrategy};
        stb.append(prefix);
        stb.append("--- Begin Solver configuration ---");
        stb.append("\n");
        for (Object o : objs) {
            stb.append(prefix);
            stb.append(o.toString());
            stb.append("\n");
        }
        stb.append(prefix);
        stb.append("timeout=");
        if (this.timeBasedTimeout) {
            stb.append(this.timeout / 1000L);
            stb.append("s\n");
        } else {
            stb.append(this.timeout);
            stb.append(" conflicts\n");
        }
        stb.append(prefix);
        stb.append("DB Simplification allowed=");
        stb.append(this.isDBSimplificationAllowed);
        stb.append("\n");
        stb.append(prefix);
        if (this.isSolverKeptHot()) {
            stb.append("Heuristics kept accross calls (keep the solver \"hot\")\n");
            stb.append(prefix);
        }
        stb.append("Listener: ");
        stb.append(this.slistener);
        stb.append("\n");
        stb.append(prefix);
        stb.append("--- End Solver configuration ---");
        return stb.toString();
    }

    public String toString() {
        return this.toString("");
    }

    @Override
    public int nextFreeVarId(boolean reserve) {
        return this.voc.nextFreeVarId(reserve);
    }

    @Override
    public IVecInt createBlockingClauseForCurrentModel() {
        VecInt clause = new VecInt(this.decisions.size());
        if (this.realNumberOfVariables() > this.nVars()) {
            for (int p : this.model) {
                clause.push(-p);
            }
        } else {
            for (int i = 0; i < this.decisions.size(); ++i) {
                clause.push(-this.decisions.get(i));
            }
        }
        return clause;
    }

    @Override
    public void unset(int p) {
        if (this.voc.isUnassigned(p) || this.trail.isEmpty()) {
            return;
        }
        int current = this.trail.last();
        while (current != p) {
            this.undoOne();
            if (this.trail.isEmpty()) {
                return;
            }
            if (!this.trailLim.isEmpty() && this.trailLim.last() == this.trail.size()) {
                this.trailLim.pop();
            }
            current = this.trail.last();
        }
        this.undoOne();
        if (!this.trailLim.isEmpty() && this.trailLim.last() == this.trail.size()) {
            this.trailLim.pop();
        }
        this.qhead = this.trail.size();
    }

    @Override
    public int getPropagationLevel() {
        return this.trail.size();
    }

    @Override
    public String getLogPrefix() {
        return this.prefix;
    }

    @Override
    public IVecInt unsatExplanation() {
        if (this.unsatExplanationInTermsOfAssumptions == null) {
            return null;
        }
        VecInt copy = new VecInt(this.unsatExplanationInTermsOfAssumptions.size());
        this.unsatExplanationInTermsOfAssumptions.copyTo(copy);
        return copy;
    }

    @Override
    public int[] modelWithInternalVariables() {
        int[] nmodel;
        if (this.model == null) {
            throw new UnsupportedOperationException("Call the solve method first!!!");
        }
        if (this.nVars() == this.realNumberOfVariables()) {
            nmodel = new int[this.model.length];
            System.arraycopy(this.model, 0, nmodel, 0, nmodel.length);
        } else {
            nmodel = new int[this.fullmodel.length];
            System.arraycopy(this.fullmodel, 0, nmodel, 0, nmodel.length);
        }
        return nmodel;
    }

    public int realNumberOfVariables() {
        return this.voc.nVars();
    }

    public int currentDecisionLevel() {
        return this.decisionLevel();
    }

    public void setNeedToReduceDB(boolean needToReduceDB) {
        this.needToReduceDB = needToReduceDB;
    }

    public boolean isSolverKeptHot() {
        return this.keepHot;
    }
}

