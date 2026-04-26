/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.discovery.DomainObject;
import net.fabricmc.loader.impl.discovery.Explanation;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.discovery.ModLoadCondition;
import net.fabricmc.loader.impl.discovery.ModResolutionException;
import net.fabricmc.loader.impl.discovery.ModResolver;
import net.fabricmc.loader.impl.lib.sat4j.pb.IPBSolver;
import net.fabricmc.loader.impl.lib.sat4j.pb.SolverFactory;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.DependencyHelper;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.INegator;
import net.fabricmc.loader.impl.lib.sat4j.pb.tools.WeightedObject;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

final class ModSolver {
    static long solverPrepTime;
    static long solveTime;
    static long solutionFetchTime;
    static long solutionAnalyzeTime;
    static long fixSetupTime;
    static long fixSolveTime;
    private static final BigInteger TWO;
    private static final INegator negator;

    ModSolver() {
    }

    static Result solve(List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<String, ModCandidateImpl> selectedMods, List<ModCandidateImpl> uniqueSelectedMods) throws ContradictionException, TimeoutException, ModResolutionException {
        IdentityHashMap<ModCandidateImpl, Integer> priorities = new IdentityHashMap<ModCandidateImpl, Integer>(allModsSorted.size());
        for (int i = 0; i < allModsSorted.size(); ++i) {
            priorities.put(allModsSorted.get(i), i);
        }
        solverPrepTime = System.nanoTime();
        IPBSolver solver = SolverFactory.newDefaultOptimizer();
        int timeout = Integer.getInteger("fabric.debug.resolutionTimeout", 60);
        if (timeout > 0) {
            solver.setTimeout(timeout);
        }
        DependencyHelper<DomainObject, Explanation> dependencyHelper = ModSolver.createDepHelper(solver);
        ModSolver.setupSolver(allModsSorted, modsById, priorities, selectedMods, uniqueSelectedMods, false, null, false, dependencyHelper);
        solveTime = System.nanoTime();
        boolean hasSolution = dependencyHelper.hasASolution();
        solutionFetchTime = System.nanoTime();
        if (hasSolution) {
            Collection<DomainObject> solution = dependencyHelper.getASolution();
            solutionAnalyzeTime = System.nanoTime();
            for (DomainObject obj : solution) {
                if (obj instanceof ModCandidateImpl) {
                    ModResolver.selectMod((ModCandidateImpl)obj, selectedMods, uniqueSelectedMods);
                    continue;
                }
                assert (obj instanceof OptionalDepVar);
            }
            dependencyHelper.reset();
            return Result.createSuccess();
        }
        Set<Explanation> reason = dependencyHelper.why();
        Set<ModDependency> failedDeps = Collections.newSetFromMap(new IdentityHashMap());
        ArrayList<Explanation> failedExplanations = new ArrayList<Explanation>();
        ModSolver.computeFailureCausesOptional(allModsSorted, modsById, priorities, selectedMods, uniqueSelectedMods, reason, dependencyHelper, failedDeps, failedExplanations);
        fixSetupTime = System.nanoTime();
        Fix fix = ModSolver.computeFix(uniqueSelectedMods, allModsSorted, modsById, priorities, selectedMods, failedDeps, dependencyHelper);
        dependencyHelper.reset();
        return Result.createFailure(reason, failedExplanations, fix);
    }

    private static void computeFailureCausesOptional(List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<ModCandidateImpl, Integer> priorities, Map<String, ModCandidateImpl> selectedMods, List<ModCandidateImpl> uniqueSelectedMods, Set<Explanation> reason, DependencyHelper<DomainObject, Explanation> dependencyHelper, Set<ModDependency> failedDeps, List<Explanation> failedExplanations) throws ContradictionException, TimeoutException {
        dependencyHelper.reset();
        dependencyHelper = ModSolver.createDepHelper(dependencyHelper.getSolver());
        ModSolver.setupSolver(allModsSorted, modsById, priorities, selectedMods, uniqueSelectedMods, true, null, false, dependencyHelper);
        if (dependencyHelper.hasASolution()) {
            Collection<DomainObject> solution = dependencyHelper.getASolution();
            HashSet<ModDependency> disabledDeps = new HashSet<ModDependency>();
            for (DomainObject obj : solution) {
                if (obj instanceof DisableDepVar) {
                    disabledDeps.add(((DisableDepVar)obj).dep);
                    continue;
                }
                assert (obj instanceof ModCandidateImpl);
            }
            for (DomainObject obj : solution) {
                if (!(obj instanceof ModCandidateImpl)) continue;
                ModCandidateImpl mod = (ModCandidateImpl)obj;
                for (ModDependency dep : mod.getDependencies()) {
                    if (!disabledDeps.contains(dep)) continue;
                    assert (dep.getKind() == ModDependency.Kind.DEPENDS || dep.getKind() == ModDependency.Kind.BREAKS);
                    failedDeps.add(dep);
                    failedExplanations.add(new Explanation(dep.getKind() == ModDependency.Kind.DEPENDS ? Explanation.ErrorKind.HARD_DEP : Explanation.ErrorKind.NEG_HARD_DEP, mod, dep));
                }
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    private static Fix computeFix(List<ModCandidateImpl> uniqueSelectedMods, List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<ModCandidateImpl, Integer> priorities, Map<String, ModCandidateImpl> selectedMods, Set<ModDependency> failedDeps, DependencyHelper<DomainObject, Explanation> dependencyHelper) throws ContradictionException, TimeoutException {
        HashMap<String, Set> depsById = new HashMap<String, Set>();
        for (ModDependency modDependency : failedDeps) {
            if (modDependency.getKind() != ModDependency.Kind.DEPENDS) continue;
            depsById.computeIfAbsent(modDependency.getModId(), ignore -> new HashSet()).add(modDependency.getVersionRequirements());
        }
        HashSet<String> modsWithOnlyOutboundDepFailures = new HashSet<String>();
        for (ModCandidateImpl modCandidateImpl : allModsSorted) {
            if (modCandidateImpl.getDependencies().isEmpty() || depsById.containsKey(modCandidateImpl.getId()) || Collections.disjoint(modCandidateImpl.getDependencies(), failedDeps)) continue;
            depsById.computeIfAbsent(modCandidateImpl.getId(), ignore -> new HashSet()).add(Collections.singleton(VersionPredicateParser.getAny()));
            modsWithOnlyOutboundDepFailures.add(modCandidateImpl.getId());
        }
        for (ModCandidateImpl modCandidateImpl : allModsSorted) {
            for (ModDependency dep : modCandidateImpl.getDependencies()) {
                Set predicates;
                if (dep.getKind() != ModDependency.Kind.DEPENDS || (predicates = (Set)depsById.get(dep.getModId())) == null) continue;
                predicates.add(dep.getVersionRequirements());
            }
        }
        HashMap<String, List<AddModVar>> hashMap = new HashMap<String, List<AddModVar>>();
        for (Map.Entry entry : depsById.entrySet()) {
            String id = (String)entry.getKey();
            boolean hadOnlyOutboundDepFailures = modsWithOnlyOutboundDepFailures.contains(id);
            HashSet<VersionInterval> allIntervals = new HashSet<VersionInterval>();
            for (Collection collection : (Set)entry.getValue()) {
                List<VersionInterval> intervals = Collections.emptyList();
                for (VersionPredicate versionPredicate : collection) {
                    intervals = VersionInterval.or(intervals, versionPredicate.getInterval());
                }
                allIntervals.addAll(intervals);
            }
            if (allIntervals.isEmpty()) continue;
            VersionInterval commonInterval = null;
            boolean bl = false;
            HashSet<Version> versions = new HashSet<Version>();
            for (VersionInterval versionInterval : allIntervals) {
                if (commonInterval == null) {
                    boolean bl2;
                    if (!bl2) {
                        commonInterval = versionInterval;
                        bl2 = true;
                    }
                } else {
                    commonInterval = versionInterval.and(commonInterval);
                }
                versions.add(ModSolver.deriveVersion(versionInterval));
            }
            List out = hashMap.computeIfAbsent(id, ignore -> new ArrayList());
            if (commonInterval != null) {
                out.add(new AddModVar(id, ModSolver.deriveVersion(commonInterval), hadOnlyOutboundDepFailures));
            } else {
                for (Version version : versions) {
                    out.add(new AddModVar(id, version, hadOnlyOutboundDepFailures));
                }
            }
            out.sort(Comparator.comparing(AddModVar::getVersion).reversed());
        }
        fixSolveTime = System.nanoTime();
        dependencyHelper.reset();
        dependencyHelper = ModSolver.createDepHelper(dependencyHelper.getSolver());
        ModSolver.setupSolver(allModsSorted, modsById, priorities, selectedMods, uniqueSelectedMods, false, hashMap, true, dependencyHelper);
        if (!dependencyHelper.hasASolution()) {
            Log.warn(LogCategory.RESOLUTION, "Unable to find a solution to fix the mod set, reason: %s", dependencyHelper.why());
            return null;
        }
        HashMap<String, ModCandidateImpl> hashMap2 = new HashMap<String, ModCandidateImpl>();
        IdentityHashMap<ModCandidateImpl, InactiveReason> identityHashMap = new IdentityHashMap<ModCandidateImpl, InactiveReason>(allModsSorted.size());
        ArrayList<AddModVar> modsToAdd = new ArrayList<AddModVar>();
        ArrayList<ModCandidateImpl> modsToRemove = new ArrayList<ModCandidateImpl>();
        HashMap<AddModVar, List<ModCandidateImpl>> modReplacements = new HashMap<AddModVar, List<ModCandidateImpl>>();
        for (ModCandidateImpl modCandidateImpl : allModsSorted) {
            identityHashMap.put(modCandidateImpl, InactiveReason.UNKNOWN);
        }
        for (DomainObject domainObject : dependencyHelper.getASolution()) {
            if (domainObject instanceof ModCandidateImpl) {
                ModCandidateImpl mod = (ModCandidateImpl)domainObject;
                hashMap2.put(mod.getId(), mod);
                identityHashMap.remove(mod);
                continue;
            }
            if (domainObject instanceof AddModVar) {
                List<ModCandidateImpl> mods;
                AddModVar mod = (AddModVar)domainObject;
                ArrayList<ModCandidateImpl> replaced = new ArrayList<ModCandidateImpl>();
                ModCandidateImpl modCandidateImpl = selectedMods.get(domainObject.getId());
                if (modCandidateImpl != null) {
                    replaced.add(modCandidateImpl);
                }
                if ((mods = modsById.get(domainObject.getId())) != null) {
                    replaced.addAll(mods);
                }
                if (replaced.isEmpty()) {
                    modsToAdd.add(mod);
                    continue;
                }
                modReplacements.put(mod, replaced);
                for (ModCandidateImpl m : replaced) {
                    identityHashMap.put(m, InactiveReason.TO_REPLACE);
                }
                continue;
            }
            if (domainObject instanceof RemoveModVar) {
                List<ModCandidateImpl> list;
                boolean found = false;
                DomainObject.Mod mod = selectedMods.get(domainObject.getId());
                if (mod != null) {
                    modsToRemove.add((ModCandidateImpl)mod);
                    identityHashMap.put((ModCandidateImpl)mod, InactiveReason.TO_REMOVE);
                    found = true;
                }
                if ((list = modsById.get(domainObject.getId())) != null) {
                    for (ModCandidateImpl m : list) {
                        if (!m.isRoot()) continue;
                        modsToRemove.add(m);
                        identityHashMap.put(m, InactiveReason.TO_REMOVE);
                        found = true;
                    }
                }
                assert (found);
                continue;
            }
            assert (false) : domainObject;
        }
        for (Collection collection : Arrays.asList(modsToAdd, modReplacements.keySet())) {
            for (DomainObject.Mod mod : collection) {
                void var19_52;
                List<VersionInterval> list = Collections.singletonList(VersionInterval.INFINITE);
                for (ModCandidateImpl m : hashMap2.values()) {
                    for (ModDependency dep : m.getDependencies()) {
                        if (!dep.getModId().equals(((AddModVar)mod).getId()) || dep.getKind().isSoft()) continue;
                        if (dep.getKind().isPositive()) {
                            List<VersionInterval> list2 = VersionInterval.and((Collection<VersionInterval>)var19_52, dep.getVersionIntervals());
                            continue;
                        }
                        List<VersionInterval> list3 = VersionInterval.and((Collection<VersionInterval>)var19_52, VersionInterval.not(dep.getVersionIntervals()));
                    }
                }
                ((AddModVar)mod).setVersionIntervals((List<VersionInterval>)var19_52);
            }
        }
        for (Map.Entry entry : identityHashMap.entrySet()) {
            if (entry.getValue() != InactiveReason.UNKNOWN) continue;
            ModCandidateImpl mod = (ModCandidateImpl)entry.getKey();
            ModCandidateImpl active = (ModCandidateImpl)hashMap2.get(mod.getId());
            if (active != null) {
                if (allModsSorted.indexOf(mod) > allModsSorted.indexOf(active)) {
                    if (mod.getVersion().equals(active.getVersion())) {
                        entry.setValue(InactiveReason.SAME_ACTIVE);
                        continue;
                    }
                    assert (mod.getVersion().compareTo(active.getVersion()) < 0);
                    entry.setValue(InactiveReason.NEWER_ACTIVE);
                    continue;
                }
                entry.setValue(InactiveReason.INCOMPATIBLE);
                continue;
            }
            if (mod.getParentMods().isEmpty()) continue;
            boolean bl = false;
            for (ModCandidateImpl m : mod.getParentMods()) {
                if (hashMap2.get(m.getId()) != m) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            entry.setValue(InactiveReason.INACTIVE_PARENT);
        }
        return new Fix(modsToAdd, modsToRemove, modReplacements, hashMap2, identityHashMap);
    }

    private static Version deriveVersion(VersionInterval interval) {
        SemanticVersion v;
        block9: {
            block10: {
                int[] comps;
                String pr;
                block12: {
                    block13: {
                        int pos;
                        block15: {
                            char c;
                            block17: {
                                block16: {
                                    String suffix;
                                    block14: {
                                        int val;
                                        block11: {
                                            block8: {
                                                if (!interval.isSemantic()) {
                                                    return interval.getMin() != null ? interval.getMin() : interval.getMax();
                                                }
                                                v = (SemanticVersion)interval.getMin();
                                                if (v == null) break block8;
                                                if (!interval.isMinInclusive()) {
                                                    String pr2 = v.getPrereleaseKey().orElse(null);
                                                    int[] comps2 = ((SemanticVersionImpl)v).getVersionComponents();
                                                    if (pr2 != null) {
                                                        pr2 = pr2.isEmpty() ? "0" : pr2.concat(".0");
                                                    } else {
                                                        if (comps2.length < 3) {
                                                            comps2 = Arrays.copyOf(comps2, comps2.length + 1);
                                                        }
                                                        int n = comps2.length - 1;
                                                        comps2[n] = comps2[n] + 1;
                                                        pr2 = "";
                                                    }
                                                    v = new SemanticVersionImpl(comps2, pr2, null);
                                                }
                                                break block9;
                                            }
                                            v = (SemanticVersion)interval.getMax();
                                            if (v == null) break block10;
                                            if (interval.isMaxInclusive()) break block9;
                                            pr = v.getPrereleaseKey().orElse(null);
                                            comps = ((SemanticVersionImpl)v).getVersionComponents();
                                            if (pr != null) break block11;
                                            pr = "zzzzzzzz";
                                            break block12;
                                        }
                                        if (pr.isEmpty()) break block13;
                                        pos = pr.lastIndexOf(46) + 1;
                                        suffix = pr.substring(pos);
                                        if (!suffix.matches("\\d+") || (val = Integer.parseInt(suffix)) <= 0) break block14;
                                        pr = pr.substring(0, pos) + (val - 1);
                                        break block12;
                                    }
                                    if (suffix.length() <= 0 || (c = suffix.charAt(suffix.length() - 1)) == '0' && suffix.length() < 2) break block15;
                                    pr = pr.substring(0, pr.length() - 1);
                                    if (c != 'a') break block16;
                                    pr = pr + 'Z';
                                    break block12;
                                }
                                if (c != 'A') break block17;
                                pr = pr + '9';
                                break block12;
                            }
                            if (c == '0') break block12;
                            pr = pr + (c - '\u0001');
                            break block12;
                        }
                        pr = pos > 0 ? pr.substring(0, pos - 1) : "";
                        break block12;
                    }
                    pr = null;
                    if (comps.length < 3) {
                        comps = Arrays.copyOf(comps, 3);
                    }
                    for (int i = 2; i >= 0; --i) {
                        if (comps[i] > 0) {
                            int n = i;
                            comps[n] = comps[n] - 1;
                            break;
                        }
                        comps[i] = 9999;
                    }
                }
                v = new SemanticVersionImpl(comps, pr, null);
                break block9;
            }
            v = new SemanticVersionImpl(new int[]{1}, null, null);
        }
        return v;
    }

    private static void setupSolver(List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<ModCandidateImpl, Integer> priorities, Map<String, ModCandidateImpl> selectedMods, List<ModCandidateImpl> uniqueSelectedMods, boolean depDisableSim, Map<String, List<AddModVar>> installableMods, boolean removalSim, DependencyHelper<DomainObject, Explanation> dependencyHelper) throws ContradictionException {
        HashMap<String, DomainObject> dummies = new HashMap<String, DomainObject>();
        HashMap<ModDependency, Map.Entry<DomainObject, Integer>> disabledDeps = depDisableSim ? new HashMap<ModDependency, Map.Entry<DomainObject, Integer>>() : null;
        ArrayList<WeightedObject<DomainObject>> weightedObjects = new ArrayList<WeightedObject<DomainObject>>();
        ModSolver.generatePreselectConstraints(uniqueSelectedMods, modsById, priorities, selectedMods, depDisableSim, installableMods, removalSim, dummies, disabledDeps, dependencyHelper, weightedObjects);
        ModSolver.generateMainConstraints(allModsSorted, modsById, priorities, selectedMods, depDisableSim, installableMods, removalSim, dummies, disabledDeps, dependencyHelper, weightedObjects);
        if (depDisableSim) {
            ModSolver.applyDisableDepVarWeights(disabledDeps, priorities.size(), weightedObjects);
        }
        WeightedObject[] weights = weightedObjects.toArray(new WeightedObject[0]);
        dependencyHelper.setObjectiveFunction(weights);
    }

    private static void generatePreselectConstraints(List<ModCandidateImpl> uniqueSelectedMods, Map<String, List<ModCandidateImpl>> modsById, Map<ModCandidateImpl, Integer> priorities, Map<String, ModCandidateImpl> selectedMods, boolean depDisableSim, Map<String, List<AddModVar>> installableMods, boolean removalSim, Map<String, DomainObject> dummyMods, Map<ModDependency, Map.Entry<DomainObject, Integer>> disabledDeps, DependencyHelper<DomainObject, Explanation> dependencyHelper, List<WeightedObject<DomainObject>> weightedObjects) throws ContradictionException {
        boolean enableOptional = !depDisableSim && installableMods == null && !removalSim;
        ArrayList<DomainObject> suitableMods = new ArrayList<DomainObject>();
        for (ModCandidateImpl mod : uniqueSelectedMods) {
            for (ModDependency dep : mod.getDependencies()) {
                if (!enableOptional && dep.getKind().isSoft() || selectedMods.containsKey(dep.getModId())) continue;
                List<DomainObject.Mod> availableMods = modsById.get(dep.getModId());
                if (availableMods != null) {
                    for (DomainObject.Mod m2 : availableMods) {
                        if (!dep.matches(m2.getVersion())) continue;
                        suitableMods.add(m2);
                    }
                }
                if (installableMods != null && (availableMods = installableMods.get(dep.getModId())) != null) {
                    for (DomainObject.Mod m2 : availableMods) {
                        if (!dep.matches(m2.getVersion())) continue;
                        suitableMods.add(m2);
                    }
                }
                if (suitableMods.isEmpty() && !depDisableSim) continue;
                switch (dep.getKind()) {
                    case DEPENDS: {
                        if (depDisableSim) {
                            suitableMods.add(ModSolver.getCreateDisableDepVar(dep, disabledDeps));
                        }
                        dependencyHelper.clause(new Explanation(Explanation.ErrorKind.PRESELECT_HARD_DEP, mod, dep), (DomainObject[])suitableMods.toArray(new DomainObject[0]));
                        break;
                    }
                    case RECOMMENDS: {
                        suitableMods.removeIf(m -> ((ModCandidateImpl)m).getLoadCondition().ordinal() > ModLoadCondition.IF_RECOMMENDED.ordinal());
                        if (suitableMods.isEmpty()) break;
                        suitableMods.add(ModSolver.getCreateDummy(dep.getModId(), OptionalDepVar::new, dummyMods, priorities.size(), weightedObjects));
                        dependencyHelper.clause(new Explanation(Explanation.ErrorKind.PRESELECT_SOFT_DEP, mod, dep), (DomainObject[])suitableMods.toArray(new DomainObject[0]));
                        break;
                    }
                    case BREAKS: {
                        if (depDisableSim) {
                            dependencyHelper.setTrue(ModSolver.getCreateDisableDepVar(dep, disabledDeps), new Explanation(Explanation.ErrorKind.PRESELECT_NEG_HARD_DEP, mod, dep));
                            break;
                        }
                        for (DomainObject match : suitableMods) {
                            dependencyHelper.setFalse(match, new Explanation(Explanation.ErrorKind.PRESELECT_NEG_HARD_DEP, mod, dep));
                        }
                        break;
                    }
                    case CONFLICTS: {
                        break;
                    }
                }
                suitableMods.clear();
            }
            if (!removalSim) continue;
            int prio = priorities.size() + 10;
            if (installableMods != null) {
                prio += installableMods.getOrDefault(mod.getId(), Collections.emptyList()).size();
                List<AddModVar> installable = installableMods.get(mod.getId());
                if (installable != null) {
                    suitableMods.addAll(installable);
                }
            }
            suitableMods.add(ModSolver.getCreateDummy(mod.getId(), RemoveModVar::new, dummyMods, prio, weightedObjects));
            suitableMods.add(mod);
            dependencyHelper.clause(new Explanation(Explanation.ErrorKind.PRESELECT_FORCELOAD, mod.getId()), (DomainObject[])suitableMods.toArray(new DomainObject[0]));
            suitableMods.clear();
        }
    }

    private static void generateMainConstraints(List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<ModCandidateImpl, Integer> priorities, Map<String, ModCandidateImpl> selectedMods, boolean depDisableSim, Map<String, List<AddModVar>> installableMods, boolean removalSim, Map<String, DomainObject> dummyMods, Map<ModDependency, Map.Entry<DomainObject, Integer>> disabledDeps, DependencyHelper<DomainObject, Explanation> dependencyHelper, List<WeightedObject<DomainObject>> weightedObjects) throws ContradictionException {
        boolean enableOptional = !depDisableSim && installableMods == null && !removalSim;
        ArrayList<DomainObject> suitableMods = new ArrayList<DomainObject>();
        for (ModCandidateImpl modCandidateImpl : allModsSorted) {
            Object availableMods;
            for (ModDependency dep : modCandidateImpl.getDependencies()) {
                Iterator<DomainObject.Mod> iterator;
                if (!enableOptional && dep.getKind().isSoft()) continue;
                ModCandidateImpl selectedMod = selectedMods.get(dep.getModId());
                if (selectedMod != null) {
                    if (!removalSim) {
                        if (dep.getKind().isSoft() || dep.matches(selectedMod.getVersion()) == dep.getKind().isPositive()) continue;
                        if (depDisableSim) {
                            dependencyHelper.setTrue(ModSolver.getCreateDisableDepVar(dep, disabledDeps), new Explanation(Explanation.ErrorKind.HARD_DEP, modCandidateImpl, dep));
                            continue;
                        }
                        dependencyHelper.setFalse(modCandidateImpl, new Explanation(Explanation.ErrorKind.HARD_DEP_INCOMPATIBLE_PRESELECTED, modCandidateImpl, dep));
                        continue;
                    }
                    if (dep.matches(selectedMod.getVersion())) {
                        suitableMods.add(selectedMod);
                    }
                }
                if ((availableMods = modsById.get(dep.getModId())) != null) {
                    iterator = availableMods.iterator();
                    while (iterator.hasNext()) {
                        DomainObject.Mod mod = iterator.next();
                        if (!dep.matches(mod.getVersion())) continue;
                        suitableMods.add(mod);
                    }
                }
                if (installableMods != null && (availableMods = installableMods.get(dep.getModId())) != null) {
                    iterator = availableMods.iterator();
                    while (iterator.hasNext()) {
                        DomainObject.Mod mod = iterator.next();
                        if (!dep.matches(mod.getVersion())) continue;
                        suitableMods.add(mod);
                    }
                }
                switch (dep.getKind()) {
                    case DEPENDS: {
                        if (depDisableSim) {
                            suitableMods.add(ModSolver.getCreateDisableDepVar(dep, disabledDeps));
                        }
                        if (suitableMods.isEmpty()) {
                            dependencyHelper.setFalse(modCandidateImpl, new Explanation(Explanation.ErrorKind.HARD_DEP_NO_CANDIDATE, modCandidateImpl, dep));
                            break;
                        }
                        dependencyHelper.implication((DomainObject[])new DomainObject[]{modCandidateImpl}).implies((DomainObject[])suitableMods.toArray(new DomainObject[0])).named(new Explanation(Explanation.ErrorKind.HARD_DEP, modCandidateImpl, dep));
                        break;
                    }
                    case RECOMMENDS: {
                        suitableMods.removeIf(m -> ((ModCandidateImpl)m).getLoadCondition().ordinal() > ModLoadCondition.IF_RECOMMENDED.ordinal());
                        if (suitableMods.isEmpty()) break;
                        suitableMods.add(ModSolver.getCreateDummy(dep.getModId(), OptionalDepVar::new, dummyMods, priorities.size(), weightedObjects));
                        dependencyHelper.implication((DomainObject[])new DomainObject[]{modCandidateImpl}).implies((DomainObject[])suitableMods.toArray(new DomainObject[0])).named(new Explanation(Explanation.ErrorKind.SOFT_DEP, modCandidateImpl, dep));
                        break;
                    }
                    case BREAKS: {
                        if (suitableMods.isEmpty()) break;
                        if (depDisableSim) {
                            DomainObject var = ModSolver.getCreateDisableDepVar(dep, disabledDeps);
                            for (DomainObject domainObject : suitableMods) {
                                dependencyHelper.implication((DomainObject[])new DomainObject[]{modCandidateImpl}).implies((DomainObject[])new DomainObject[]{new NegatedDomainObject(domainObject), var}).named(new Explanation(Explanation.ErrorKind.NEG_HARD_DEP, modCandidateImpl, dep));
                            }
                        } else {
                            for (DomainObject domainObject : suitableMods) {
                                dependencyHelper.implication((DomainObject[])new DomainObject[]{modCandidateImpl}).impliesNot(domainObject).named(new Explanation(Explanation.ErrorKind.NEG_HARD_DEP, modCandidateImpl, dep));
                            }
                        }
                        break;
                    }
                    case CONFLICTS: {
                        break;
                    }
                }
                suitableMods.clear();
            }
            if (!modCandidateImpl.isRoot()) {
                ModLoadCondition loadCondition = modCandidateImpl.getLoadCondition();
                if (loadCondition == ModLoadCondition.ALWAYS) {
                    Explanation explanation = new Explanation(Explanation.ErrorKind.NESTED_FORCELOAD, modCandidateImpl.getParentMods().iterator().next(), modCandidateImpl.getId());
                    DomainObject[] siblings = modsById.get(modCandidateImpl.getId()).toArray(new DomainObject[0]);
                    if (ModSolver.isAnyParentSelected(modCandidateImpl, selectedMods)) {
                        dependencyHelper.clause(explanation, (DomainObject[])siblings);
                    } else {
                        availableMods = modCandidateImpl.getParentMods().iterator();
                        while (availableMods.hasNext()) {
                            ModCandidateImpl parent = (ModCandidateImpl)availableMods.next();
                            dependencyHelper.implication((DomainObject[])new DomainObject[]{parent}).implies((DomainObject[])siblings).named(explanation);
                        }
                    }
                }
                if (!ModSolver.isAnyParentSelected(modCandidateImpl, selectedMods)) {
                    dependencyHelper.implication((DomainObject[])new DomainObject[]{modCandidateImpl}).implies((DomainObject[])modCandidateImpl.getParentMods().toArray(new DomainObject[0])).named(new Explanation(Explanation.ErrorKind.NESTED_REQ_PARENT, modCandidateImpl));
                }
            }
            if (modCandidateImpl.isRoot() && modCandidateImpl.getLoadCondition() == ModLoadCondition.ALWAYS && modsById.get(modCandidateImpl.getId()).size() <= 1) continue;
            int prio = priorities.get(modCandidateImpl);
            BigInteger weight = modCandidateImpl.getLoadCondition().ordinal() >= ModLoadCondition.IF_RECOMMENDED.ordinal() ? TWO.pow(prio + 1) : TWO.pow(allModsSorted.size() - prio).negate();
            weightedObjects.add(WeightedObject.newWO(modCandidateImpl, weight));
        }
        for (List list : modsById.values()) {
            List<AddModVar> installable;
            ModCandidateImpl firstMod = (ModCandidateImpl)list.get(0);
            String id = firstMod.getId();
            if (list.size() == 1 && !removalSim) {
                if (firstMod.isRoot() && firstMod.getLoadCondition() == ModLoadCondition.ALWAYS) {
                    dependencyHelper.setTrue(firstMod, new Explanation(Explanation.ErrorKind.ROOT_FORCELOAD_SINGLE, firstMod));
                }
            } else {
                boolean isRequired = false;
                for (ModCandidateImpl mod : list) {
                    if (!mod.isRoot() || mod.getLoadCondition() != ModLoadCondition.ALWAYS) continue;
                    isRequired = true;
                    break;
                }
                if (isRequired) {
                    List<AddModVar> installable2;
                    if (removalSim) {
                        int prio = priorities.size() + 10;
                        if (installableMods != null) {
                            prio += installableMods.getOrDefault(id, Collections.emptyList()).size();
                        }
                        suitableMods.add(ModSolver.getCreateDummy(id, RemoveModVar::new, dummyMods, prio, weightedObjects));
                    }
                    if (installableMods != null && (installable2 = installableMods.get(id)) != null) {
                        suitableMods.addAll(installable2);
                    }
                    suitableMods.addAll(list);
                    dependencyHelper.clause(new Explanation(Explanation.ErrorKind.ROOT_FORCELOAD, id), (DomainObject[])suitableMods.toArray(new DomainObject[0]));
                    suitableMods.clear();
                }
            }
            suitableMods.addAll(list);
            if (installableMods != null && (installable = installableMods.get(id)) != null && !installable.isEmpty()) {
                suitableMods.addAll(installable);
                ModCandidateImpl mod = selectedMods.get(id);
                if (mod != null) {
                    suitableMods.add(mod);
                }
            }
            if (suitableMods.size() > 1 || enableOptional && firstMod.getLoadCondition() == ModLoadCondition.IF_POSSIBLE) {
                dependencyHelper.atMost(1, (DomainObject[])suitableMods.toArray(new DomainObject[0])).named(new Explanation(Explanation.ErrorKind.UNIQUE_ID, id));
            }
            suitableMods.clear();
        }
        if (installableMods != null) {
            for (List list : installableMods.values()) {
                String id = ((AddModVar)list.get(0)).getId();
                boolean isReplacement = modsById.containsKey(id);
                if (!isReplacement) {
                    suitableMods.addAll(list);
                    ModCandidateImpl selectedMod = selectedMods.get(id);
                    if (selectedMod != null) {
                        suitableMods.add(selectedMod);
                    }
                    if (suitableMods.size() > 1) {
                        dependencyHelper.atMost(1, (DomainObject[])suitableMods.toArray(new DomainObject[0])).named(new Explanation(Explanation.ErrorKind.UNIQUE_ID, id));
                    }
                    suitableMods.clear();
                }
                for (int i = 0; i < list.size(); ++i) {
                    AddModVar mod = (AddModVar)list.get(i);
                    int weight = priorities.size() + 4 + i;
                    if (isReplacement) {
                        weight += 3;
                    }
                    if (mod.hadOnlyOutboundDepFailures) {
                        ++weight;
                    }
                    weightedObjects.add(WeightedObject.newWO(mod, TWO.pow(weight)));
                }
            }
        }
    }

    private static DependencyHelper<DomainObject, Explanation> createDepHelper(IPBSolver solver) {
        DependencyHelper<DomainObject, Explanation> ret = new DependencyHelper<DomainObject, Explanation>(solver);
        ret.setNegator(negator);
        return ret;
    }

    private static DomainObject getCreateDummy(String id, Function<String, DomainObject> supplier, Map<String, DomainObject> duplicateMap, int modCount, List<WeightedObject<DomainObject>> weightedObjects) {
        DomainObject ret = duplicateMap.get(id);
        if (ret != null) {
            return ret;
        }
        ret = supplier.apply(id);
        int weight = modCount + 2;
        weightedObjects.add(WeightedObject.newWO(ret, TWO.pow(weight)));
        return ret;
    }

    private static DomainObject getCreateDisableDepVar(ModDependency dep, Map<ModDependency, Map.Entry<DomainObject, Integer>> duplicateMap) {
        Map.Entry entry = duplicateMap.computeIfAbsent(dep, d -> new AbstractMap.SimpleEntry<DisableDepVar, Integer>(new DisableDepVar((ModDependency)d), 0));
        entry.setValue((Integer)entry.getValue() + 1);
        return (DomainObject)entry.getKey();
    }

    private static void applyDisableDepVarWeights(Map<ModDependency, Map.Entry<DomainObject, Integer>> map, int modCount, List<WeightedObject<DomainObject>> weightedObjects) {
        BigInteger baseWeight = TWO.pow(modCount + 3);
        Iterator<Map.Entry<DomainObject, Integer>> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            int count;
            Map.Entry<DomainObject, Integer> entry;
            weightedObjects.add(WeightedObject.newWO(entry.getKey(), (count = (entry = iterator.next()).getValue().intValue()) > 1 ? baseWeight.multiply(BigInteger.valueOf(count)) : baseWeight));
        }
    }

    static boolean isAnyParentSelected(ModCandidateImpl mod, Map<String, ModCandidateImpl> selectedMods) {
        for (ModCandidateImpl parentMod : mod.getParentMods()) {
            if (selectedMods.get(parentMod.getId()) != parentMod) continue;
            return true;
        }
        return false;
    }

    static boolean hasAllDepsSatisfied(ModCandidateImpl mod, Map<String, ModCandidateImpl> mods) {
        for (ModDependency dep : mod.getDependencies()) {
            ModCandidateImpl m;
            if (!(dep.getKind() == ModDependency.Kind.DEPENDS ? (m = mods.get(dep.getModId())) == null || !dep.matches(m.getVersion()) : dep.getKind() == ModDependency.Kind.BREAKS && (m = mods.get(dep.getModId())) != null && dep.matches(m.getVersion()))) continue;
            return false;
        }
        return true;
    }

    static {
        TWO = BigInteger.valueOf(2L);
        negator = new INegator(){

            @Override
            public Object unNegate(Object thing) {
                return ((NegatedDomainObject)thing).obj;
            }

            @Override
            public boolean isNegated(Object thing) {
                return thing instanceof NegatedDomainObject;
            }
        };
    }

    private static final class OptionalDepVar
    implements DomainObject {
        private final String id;

        OptionalDepVar(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public String toString() {
            return "optionalDep:" + this.getId();
        }
    }

    static class Result {
        final boolean success;
        final Collection<Explanation> immediateReason;
        final Collection<Explanation> reason;
        final Fix fix;

        static Result createSuccess() {
            return new Result(true, null, null, null);
        }

        static Result createFailure(Collection<Explanation> immediateReason, Collection<Explanation> reason, Fix fix) {
            return new Result(false, immediateReason, reason, fix);
        }

        private Result(boolean success, Collection<Explanation> immediateReason, Collection<Explanation> reason, Fix fix) {
            this.success = success;
            this.immediateReason = immediateReason;
            this.reason = reason;
            this.fix = fix;
        }
    }

    static class Fix {
        final Collection<AddModVar> modsToAdd;
        final Collection<ModCandidateImpl> modsToRemove;
        final Map<AddModVar, List<ModCandidateImpl>> modReplacements;
        final Map<String, ModCandidateImpl> activeMods;
        final Map<ModCandidateImpl, InactiveReason> inactiveMods;

        Fix(Collection<AddModVar> modsToAdd, Collection<ModCandidateImpl> modsToRemove, Map<AddModVar, List<ModCandidateImpl>> modReplacements, Map<String, ModCandidateImpl> activeMods, Map<ModCandidateImpl, InactiveReason> inactiveMods) {
            this.modsToAdd = modsToAdd;
            this.modsToRemove = modsToRemove;
            this.modReplacements = modReplacements;
            this.activeMods = activeMods;
            this.inactiveMods = inactiveMods;
        }
    }

    private static final class DisableDepVar
    implements DomainObject {
        final ModDependency dep;

        DisableDepVar(ModDependency dep) {
            this.dep = dep;
        }

        @Override
        public String getId() {
            return this.dep.getModId();
        }

        public String toString() {
            return "disableDep:" + this.dep;
        }
    }

    static final class AddModVar
    implements DomainObject.Mod {
        private final String id;
        private final Version version;
        final boolean hadOnlyOutboundDepFailures;
        private List<VersionInterval> versionIntervals;

        AddModVar(String id, Version version, boolean hadOnlyOutboundDepFailures) {
            this.id = id;
            this.version = version;
            this.hadOnlyOutboundDepFailures = hadOnlyOutboundDepFailures;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public Version getVersion() {
            return this.version;
        }

        public List<VersionInterval> getVersionIntervals() {
            return this.versionIntervals;
        }

        void setVersionIntervals(List<VersionInterval> versionIntervals) {
            this.versionIntervals = versionIntervals;
        }

        public String toString() {
            return String.format("add:%s %s (%s)", this.id, this.version, this.versionIntervals);
        }
    }

    static enum InactiveReason {
        INACTIVE_PARENT("inactive_parent"),
        INCOMPATIBLE("incompatible"),
        NEWER_ACTIVE("newer_active"),
        SAME_ACTIVE("same_active"),
        TO_REMOVE("to_remove"),
        TO_REPLACE("to_replace"),
        UNKNOWN("unknown"),
        WRONG_ENVIRONMENT("wrong_environment");

        final String id;

        private InactiveReason(String id) {
            this.id = id;
        }
    }

    private static final class RemoveModVar
    implements DomainObject {
        private final String id;

        RemoveModVar(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public String toString() {
            return "remove:" + this.getId();
        }
    }

    private static final class NegatedDomainObject
    implements DomainObject {
        private final DomainObject obj;

        NegatedDomainObject(DomainObject obj) {
            this.obj = obj;
        }

        @Override
        public String getId() {
            return this.obj.getId();
        }

        public String toString() {
            return "!" + this.obj;
        }
    }
}

