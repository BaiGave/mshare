/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.impl.discovery.Explanation;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.discovery.ModSolver;
import net.fabricmc.loader.impl.util.Localization;
import net.fabricmc.loader.impl.util.StringUtil;
import net.fabricmc.loader.impl.util.version.VersionIntervalImpl;

final class ResultAnalyzer {
    private static final boolean SHOW_PATH_INFO = false;
    private static final boolean SHOW_INACTIVE = false;

    ResultAnalyzer() {
    }

    static String gatherErrors(ModSolver.Result result, Map<String, ModCandidateImpl> selectedMods, Map<String, List<ModCandidateImpl>> modsById, Map<String, Set<ModCandidateImpl>> envDisabledMods, EnvType envType) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw);){
            String prefix = "";
            boolean suggestFix = true;
            if (result.fix != null) {
                pw.printf("\n%s", Localization.format("resolution.solutionHeader", new Object[0]));
                ResultAnalyzer.formatFix(result.fix, result, selectedMods, modsById, envDisabledMods, envType, pw);
                pw.printf("\n%s", Localization.format("resolution.depListHeader", new Object[0]));
                prefix = "\t";
                suggestFix = false;
            }
            ArrayList<ModCandidateImpl> matches = new ArrayList<ModCandidateImpl>();
            for (Explanation explanation : result.reason) {
                assert (explanation.error.isDependencyError);
                ModDependency dep = explanation.dep;
                ModCandidateImpl selected = selectedMods.get(dep.getModId());
                if (selected != null) {
                    matches.add(selected);
                } else {
                    List<ModCandidateImpl> candidates = modsById.get(dep.getModId());
                    if (candidates != null) {
                        matches.addAll(candidates);
                    }
                }
                ResultAnalyzer.addErrorToList(explanation.mod, explanation.dep, matches, envDisabledMods.containsKey(dep.getModId()), suggestFix, prefix, pw);
                matches.clear();
            }
        }
        return sw.toString();
    }

    private static void formatFix(ModSolver.Fix fix, ModSolver.Result result, Map<String, ModCandidateImpl> selectedMods, Map<String, List<ModCandidateImpl>> modsById, Map<String, Set<ModCandidateImpl>> envDisabledMods, EnvType envType, PrintWriter pw) {
        for (ModSolver.AddModVar addModVar : fix.modsToAdd) {
            Set<ModCandidateImpl> envDisabledAlternatives = envDisabledMods.get(addModVar.getId());
            if (envDisabledAlternatives == null) {
                pw.printf("\n\t - %s", Localization.format("resolution.solution.addMod", addModVar.getId(), ResultAnalyzer.formatVersionRequirements(addModVar.getVersionIntervals())));
                continue;
            }
            String envKey = String.format("environment.%s", envType.name().toLowerCase(Locale.ENGLISH));
            pw.printf("\n\t - %s", Localization.format("resolution.solution.replaceModEnvDisabled", ResultAnalyzer.formatOldMods(envDisabledAlternatives), addModVar.getId(), ResultAnalyzer.formatVersionRequirements(addModVar.getVersionIntervals()), Localization.format(envKey, new Object[0])));
        }
        for (ModCandidateImpl modCandidateImpl : fix.modsToRemove) {
            pw.printf("\n\t - %s", Localization.format("resolution.solution.removeMod", ResultAnalyzer.getName(modCandidateImpl), ResultAnalyzer.getVersion(modCandidateImpl), modCandidateImpl.getLocalPath()));
        }
        for (Map.Entry entry : fix.modReplacements.entrySet()) {
            boolean hasOverlap;
            ModSolver.AddModVar newMod = (ModSolver.AddModVar)entry.getKey();
            List oldMods = (List)entry.getValue();
            String oldModsFormatted = ResultAnalyzer.formatOldMods(oldMods);
            if (oldMods.size() != 1 || !((ModCandidateImpl)oldMods.get(0)).getId().equals(newMod.getId())) {
                String newModName = newMod.getId();
                ModCandidateImpl alt = selectedMods.get(newMod.getId());
                if (alt != null) {
                    newModName = ResultAnalyzer.getName(alt);
                } else {
                    List<ModCandidateImpl> alts = modsById.get(newMod.getId());
                    if (alts != null && !alts.isEmpty()) {
                        newModName = ResultAnalyzer.getName(alts.get(0));
                    }
                }
                pw.printf("\n\t - %s", Localization.format("resolution.solution.replaceMod", oldModsFormatted, newModName, ResultAnalyzer.formatVersionRequirements(newMod.getVersionIntervals())));
                continue;
            }
            ModCandidateImpl oldMod = (ModCandidateImpl)oldMods.get(0);
            boolean bl = hasOverlap = !VersionInterval.and(newMod.getVersionIntervals(), Collections.singletonList(new VersionIntervalImpl(oldMod.getVersion(), true, oldMod.getVersion(), true))).isEmpty();
            if (!hasOverlap) {
                pw.printf("\n\t - %s", Localization.format("resolution.solution.replaceModVersion", oldModsFormatted, ResultAnalyzer.formatVersionRequirements(newMod.getVersionIntervals())));
                continue;
            }
            pw.printf("\n\t - %s", Localization.format("resolution.solution.replaceModVersionDifferent", oldModsFormatted, ResultAnalyzer.formatVersionRequirements(newMod.getVersionIntervals())));
            boolean foundAny = false;
            block3: for (ModDependency dep : oldMod.getDependencies()) {
                if (dep.getKind().isSoft()) continue;
                ModCandidateImpl mod = fix.activeMods.get(dep.getModId());
                if (mod != null) {
                    if (dep.matches(mod.getVersion()) == dep.getKind().isPositive()) continue;
                    pw.printf("\n\t\t - %s", Localization.format("resolution.solution.replaceModVersionDifferent.reqSupportedModVersion", mod.getId(), ResultAnalyzer.getVersion(mod)));
                    foundAny = true;
                    continue;
                }
                for (ModSolver.AddModVar addMod : fix.modReplacements.keySet()) {
                    if (!addMod.getId().equals(dep.getModId())) continue;
                    pw.printf("\n\t\t - %s", Localization.format("resolution.solution.replaceModVersionDifferent.reqSupportedModVersions", addMod.getId(), ResultAnalyzer.formatVersionRequirements(addMod.getVersionIntervals())));
                    foundAny = true;
                    continue block3;
                }
            }
            if (foundAny) continue;
            pw.printf("\n\t\t - %s", Localization.format("resolution.solution.replaceModVersionDifferent.unknown", new Object[0]));
        }
    }

    static String gatherWarnings(List<ModCandidateImpl> uniqueSelectedMods, Map<String, ModCandidateImpl> selectedMods, Map<String, Set<ModCandidateImpl>> envDisabledMods, EnvType envType) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw);){
            for (ModCandidateImpl mod : uniqueSelectedMods) {
                for (ModDependency dep : mod.getDependencies()) {
                    switch (dep.getKind()) {
                        case RECOMMENDS: {
                            ModCandidateImpl depMod = selectedMods.get(dep.getModId());
                            if (depMod != null && dep.matches(depMod.getVersion())) break;
                            ResultAnalyzer.addErrorToList(mod, dep, ResultAnalyzer.toList(depMod), envDisabledMods.containsKey(dep.getModId()), true, "", pw);
                            break;
                        }
                        case CONFLICTS: {
                            ModCandidateImpl depMod = selectedMods.get(dep.getModId());
                            if (depMod == null || !dep.matches(depMod.getVersion())) break;
                            ResultAnalyzer.addErrorToList(mod, dep, ResultAnalyzer.toList(depMod), false, true, "", pw);
                            break;
                        }
                    }
                }
            }
        }
        if (sw.getBuffer().length() == 0) {
            return null;
        }
        return sw.toString();
    }

    private static List<ModCandidateImpl> toList(ModCandidateImpl mod) {
        return mod != null ? Collections.singletonList(mod) : Collections.emptyList();
    }

    private static void addErrorToList(ModCandidateImpl mod, ModDependency dep, List<ModCandidateImpl> matches, boolean presentForOtherEnv, boolean suggestFix, String prefix, PrintWriter pw) {
        String reason;
        Object[] args = new Object[]{ResultAnalyzer.getName(mod), ResultAnalyzer.getVersion(mod), matches.isEmpty() ? dep.getModId() : ResultAnalyzer.getName(matches.get(0)), ResultAnalyzer.formatVersionRequirements(dep.getVersionIntervals()), ResultAnalyzer.getVersions(matches), matches.size()};
        if (!matches.isEmpty()) {
            boolean present;
            if (dep.getKind().isPositive()) {
                present = false;
                for (ModCandidateImpl match : matches) {
                    if (!dep.matches(match.getVersion())) continue;
                    present = true;
                    break;
                }
            } else {
                present = true;
            }
            reason = present ? "invalid" : "mismatch";
        } else {
            reason = presentForOtherEnv && dep.getKind().isPositive() ? "envDisabled" : "missing";
        }
        String key = String.format("resolution.%s.%s", dep.getKind().getKey(), reason);
        pw.printf("\n%s - %s", prefix, StringUtil.capitalize(Localization.format(key, args)));
        if (suggestFix) {
            key = String.format("resolution.%s.suggestion", dep.getKind().getKey());
            pw.printf("\n%s\t - %s", prefix, StringUtil.capitalize(Localization.format(key, args)));
        }
    }

    private static void appendJijInfo(ModCandidateImpl mod, String prefix, boolean mentionMod, PrintWriter pw) {
        String path;
        String loc;
        if (mod.getMetadata().getType().equals("builtin")) {
            loc = "builtin";
            path = null;
        } else if (mod.isRoot()) {
            loc = "root";
            path = mod.getLocalPath();
        } else {
            ModCandidateImpl best;
            loc = "normal";
            ArrayList<ModCandidateImpl> paths = new ArrayList<ModCandidateImpl>();
            paths.add(mod);
            ModCandidateImpl cur = mod;
            do {
                best = null;
                int maxDiff = 0;
                for (ModCandidateImpl parent : cur.getParentMods()) {
                    int diff = cur.getMinNestLevel() - parent.getMinNestLevel();
                    if (diff <= maxDiff) continue;
                    best = parent;
                    maxDiff = diff;
                }
                if (best == null) break;
                paths.add(best);
            } while (!(cur = best).isRoot());
            StringBuilder pathSb = new StringBuilder();
            for (int i = paths.size() - 1; i >= 0; --i) {
                ModCandidateImpl m = (ModCandidateImpl)paths.get(i);
                if (pathSb.length() > 0) {
                    pathSb.append(" -> ");
                }
                pathSb.append(m.getLocalPath());
            }
            path = pathSb.toString();
        }
        String key = String.format("resolution.jij.%s%s", loc, mentionMod ? "" : "NoMention");
        String text = mentionMod ? (path == null ? Localization.format(key, ResultAnalyzer.getName(mod), ResultAnalyzer.getVersion(mod)) : Localization.format(key, ResultAnalyzer.getName(mod), ResultAnalyzer.getVersion(mod), path)) : (path == null ? Localization.format(key, new Object[0]) : Localization.format(key, path));
        pw.printf("\n%s\t - %s", prefix, StringUtil.capitalize(text));
    }

    private static String formatOldMods(Collection<ModCandidateImpl> mods) {
        ArrayList<ModCandidateImpl> modsSorted = new ArrayList<ModCandidateImpl>(mods);
        modsSorted.sort(ModCandidateImpl.ID_VERSION_COMPARATOR);
        ArrayList<String> ret = new ArrayList<String>(modsSorted.size());
        for (ModCandidateImpl m : modsSorted) {
            ret.add(Localization.format("resolution.solution.replaceMod.oldModNoPath", ResultAnalyzer.getName(m), ResultAnalyzer.getVersion(m)));
        }
        return ResultAnalyzer.formatEnumeration(ret, true);
    }

    private static String getName(ModCandidateImpl candidate) {
        String typePrefix;
        switch (candidate.getMetadata().getType()) {
            case "fabric": {
                typePrefix = String.format("%s ", Localization.format("resolution.type.mod", new Object[0]));
                break;
            }
            default: {
                typePrefix = "";
            }
        }
        return String.format("%s'%s' (%s)", typePrefix, candidate.getMetadata().getName(), candidate.getId());
    }

    private static String getVersion(ModCandidateImpl candidate) {
        return candidate.getVersion().getFriendlyString();
    }

    private static String getVersions(Collection<ModCandidateImpl> candidates) {
        return candidates.stream().map(ResultAnalyzer::getVersion).collect(Collectors.joining("/"));
    }

    private static String formatVersionRequirements(Collection<VersionInterval> intervals) {
        ArrayList<String> ret = new ArrayList<String>();
        for (VersionInterval interval : intervals) {
            SemanticVersion version;
            String str;
            if (interval == null) continue;
            if (interval.getMin() == null) {
                if (interval.getMax() == null) {
                    return Localization.format("resolution.version.any", new Object[0]);
                }
                str = interval.isMaxInclusive() ? Localization.format("resolution.version.lessEqual", interval.getMax()) : Localization.format("resolution.version.less", interval.getMax());
            } else if (interval.getMax() == null) {
                str = interval.isMinInclusive() ? Localization.format("resolution.version.greaterEqual", interval.getMin()) : Localization.format("resolution.version.greater", interval.getMin());
            } else if (interval.getMin().equals(interval.getMax())) {
                if (!interval.isMinInclusive() || !interval.isMaxInclusive()) continue;
                str = Localization.format("resolution.version.equal", interval.getMin());
            } else if (ResultAnalyzer.isWildcard(interval, 0)) {
                version = (SemanticVersion)interval.getMin();
                str = Localization.format("resolution.version.major", version.getVersionComponent(0));
            } else if (ResultAnalyzer.isWildcard(interval, 1)) {
                version = (SemanticVersion)interval.getMin();
                str = Localization.format("resolution.version.majorMinor", version.getVersionComponent(0), version.getVersionComponent(1));
            } else {
                String key = String.format("resolution.version.rangeMin%sMax%s", interval.isMinInclusive() ? "Inc" : "Exc", interval.isMaxInclusive() ? "Inc" : "Exc");
                str = Localization.format(key, interval.getMin(), interval.getMax());
            }
            ret.add(str);
        }
        if (ret.isEmpty()) {
            return Localization.format("resolution.version.none", new Object[0]);
        }
        return ResultAnalyzer.formatEnumeration(ret, false);
    }

    private static boolean isWildcard(VersionInterval interval, int incrementedComponent) {
        if (interval == null || interval.getMin() == null || interval.getMax() == null || !interval.isMinInclusive() || interval.isMaxInclusive() || !interval.isSemantic()) {
            return false;
        }
        SemanticVersion min = (SemanticVersion)interval.getMin();
        SemanticVersion max = (SemanticVersion)interval.getMax();
        if (!"".equals(min.getPrereleaseKey().orElse(null)) || !"".equals(max.getPrereleaseKey().orElse(null))) {
            return false;
        }
        if (max.getVersionComponent(incrementedComponent) != min.getVersionComponent(incrementedComponent) + 1) {
            return false;
        }
        int m = Math.max(min.getVersionComponentCount(), max.getVersionComponentCount());
        for (int i = incrementedComponent + 1; i < m; ++i) {
            if (min.getVersionComponent(i) == 0 && max.getVersionComponent(i) == 0) continue;
            return false;
        }
        return true;
    }

    private static String formatEnumeration(Collection<?> elements, boolean isAnd) {
        String keyPrefix = isAnd ? "enumerationAnd." : "enumerationOr.";
        Iterator<?> it = elements.iterator();
        switch (elements.size()) {
            case 0: {
                return "";
            }
            case 1: {
                return Objects.toString(it.next());
            }
            case 2: {
                return Localization.format(keyPrefix + "2", it.next(), it.next());
            }
            case 3: {
                return Localization.format(keyPrefix + "3", it.next(), it.next(), it.next());
            }
        }
        String ret = Localization.format(keyPrefix + "nPrefix", it.next());
        do {
            Object next = it.next();
            ret = Localization.format(it.hasNext() ? keyPrefix + "n" : keyPrefix + "nSuffix", ret, next);
        } while (it.hasNext());
        return ret;
    }
}

