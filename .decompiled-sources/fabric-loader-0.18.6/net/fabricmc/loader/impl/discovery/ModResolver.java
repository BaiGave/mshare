/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;
import net.fabricmc.loader.impl.discovery.ModPrioSorter;
import net.fabricmc.loader.impl.discovery.ModResolutionException;
import net.fabricmc.loader.impl.discovery.ModSolver;
import net.fabricmc.loader.impl.discovery.ResultAnalyzer;
import net.fabricmc.loader.impl.lib.sat4j.specs.ContradictionException;
import net.fabricmc.loader.impl.lib.sat4j.specs.TimeoutException;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

public class ModResolver {
    public static List<ModCandidateImpl> resolve(Collection<ModCandidateImpl> candidates, EnvType envType, Map<String, Set<ModCandidateImpl>> envDisabledMods) throws ModResolutionException {
        long startTime = System.nanoTime();
        List<ModCandidateImpl> result = ModResolver.findCompatibleSet(candidates, envType, envDisabledMods);
        long endTime = System.nanoTime();
        Log.debug(LogCategory.RESOLUTION, "Mod resolution time: %.1f ms", (double)(endTime - startTime) * 1.0E-6);
        return result;
    }

    private static List<ModCandidateImpl> findCompatibleSet(Collection<ModCandidateImpl> candidates, EnvType envType, Map<String, Set<ModCandidateImpl>> envDisabledMods) throws ModResolutionException {
        ModCandidateImpl modCandidateImpl;
        ModSolver.Result result;
        ArrayList<ModCandidateImpl> allModsSorted = new ArrayList<ModCandidateImpl>(candidates);
        LinkedHashMap<String, List<ModCandidateImpl>> modsById = new LinkedHashMap<String, List<ModCandidateImpl>>();
        ModPrioSorter.sort(allModsSorted, modsById);
        for (ModCandidateImpl modCandidateImpl2 : allModsSorted) {
            if (modCandidateImpl2.getMetadata().getSchemaVersion() >= 2) continue;
            block3: for (ModDependency dep : modCandidateImpl2.getMetadata().getDependencies()) {
                Collection collection;
                if (!dep.getKind().isPositive() || dep.getKind() == ModDependency.Kind.SUGGESTS || !(dep instanceof ModDependencyImpl) || modsById.containsKey(dep.getModId()) || (collection = (Collection)envDisabledMods.get(dep.getModId())) == null) continue;
                for (Object m : collection) {
                    if (!dep.matches(((ModCandidateImpl)m).getVersion())) continue;
                    ((ModDependencyImpl)dep).setKind(ModDependency.Kind.SUGGESTS);
                    continue block3;
                }
            }
        }
        ArrayList preselectedMods = new ArrayList();
        for (List mods : modsById.values()) {
            Object builtinMod = null;
            for (ModCandidateImpl modCandidateImpl3 : mods) {
                if (!modCandidateImpl3.isBuiltin()) continue;
                builtinMod = modCandidateImpl3;
                break;
            }
            if (builtinMod == null) continue;
            if (mods.size() > 1) {
                mods.remove(builtinMod);
                throw new ModResolutionException("Mods share ID with builtin mod %s: %s", builtinMod, mods);
            }
            preselectedMods.add(builtinMod);
        }
        HashMap<String, ModCandidateImpl> hashMap = new HashMap<String, ModCandidateImpl>(allModsSorted.size());
        ArrayList<ModCandidateImpl> uniqueSelectedMods = new ArrayList<ModCandidateImpl>(allModsSorted.size());
        for (ModCandidateImpl modCandidateImpl4 : preselectedMods) {
            ModResolver.preselectMod(modCandidateImpl4, allModsSorted, modsById, hashMap, uniqueSelectedMods);
        }
        try {
            result = ModSolver.solve(allModsSorted, modsById, hashMap, uniqueSelectedMods);
        }
        catch (ContradictionException | TimeoutException exception) {
            throw new ModResolutionException("Solving failed", exception);
        }
        if (!result.success) {
            Log.warn(LogCategory.RESOLUTION, "Mod resolution failed");
            Log.info(LogCategory.RESOLUTION, "Immediate reason: %s%n", result.immediateReason);
            Log.info(LogCategory.RESOLUTION, "Reason: %s%n", result.reason);
            if (!envDisabledMods.isEmpty()) {
                Log.info(LogCategory.RESOLUTION, "%s environment disabled: %s%n", envType.name(), envDisabledMods.keySet());
            }
            if (result.fix == null) {
                Log.info(LogCategory.RESOLUTION, "No fix?");
            } else {
                Log.info(LogCategory.RESOLUTION, "Fix: add %s, remove %s, replace [%s]%n", result.fix.modsToAdd, result.fix.modsToRemove, result.fix.modReplacements.entrySet().stream().map(e -> String.format("%s -> %s", e.getValue(), e.getKey())).collect(Collectors.joining(", ")));
                for (Collection collection : envDisabledMods.values()) {
                    for (ModCandidateImpl m2 : collection) {
                        result.fix.inactiveMods.put(m2, ModSolver.InactiveReason.WRONG_ENVIRONMENT);
                    }
                }
            }
            throw new ModResolutionException("Some of your mods are incompatible with the game or each other!%s", ResultAnalyzer.gatherErrors(result, hashMap, modsById, envDisabledMods, envType));
        }
        uniqueSelectedMods.sort(Comparator.comparing(ModCandidateImpl::getId));
        ArrayDeque<ModCandidateImpl> arrayDeque = new ArrayDeque<ModCandidateImpl>();
        for (ModCandidateImpl mod5 : allModsSorted) {
            if (hashMap.get(mod5.getId()) == mod5) {
                if (mod5.resetMinNestLevel()) continue;
                arrayDeque.add(mod5);
                continue;
            }
            mod5.clearCachedData();
            for (ModCandidateImpl m3 : mod5.getNestedMods()) {
                m3.getParentMods().remove(mod5);
            }
            for (ModCandidateImpl m : mod5.getParentMods()) {
                m.getNestedMods().remove(mod5);
            }
        }
        while ((modCandidateImpl = (ModCandidateImpl)arrayDeque.poll()) != null) {
            for (ModCandidateImpl child : modCandidateImpl.getNestedMods()) {
                if (!child.updateMinNestLevel(modCandidateImpl)) continue;
                arrayDeque.add(child);
            }
        }
        String string = ResultAnalyzer.gatherWarnings(uniqueSelectedMods, hashMap, envDisabledMods, envType);
        if (string != null) {
            Log.warn(LogCategory.RESOLUTION, "Warnings were found!%s", string);
        }
        return uniqueSelectedMods;
    }

    static void preselectMod(ModCandidateImpl mod, List<ModCandidateImpl> allModsSorted, Map<String, List<ModCandidateImpl>> modsById, Map<String, ModCandidateImpl> selectedMods, List<ModCandidateImpl> uniqueSelectedMods) throws ModResolutionException {
        ModResolver.selectMod(mod, selectedMods, uniqueSelectedMods);
        allModsSorted.removeAll((Collection)modsById.remove(mod.getId()));
        for (String provided : mod.getProvides()) {
            allModsSorted.removeAll((Collection)modsById.remove(provided));
        }
    }

    static void selectMod(ModCandidateImpl mod, Map<String, ModCandidateImpl> selectedMods, List<ModCandidateImpl> uniqueSelectedMods) throws ModResolutionException {
        ModCandidateImpl prev = selectedMods.put(mod.getId(), mod);
        if (prev != null) {
            throw new ModResolutionException("duplicate mod %s", mod.getId());
        }
        for (String provided : mod.getProvides()) {
            prev = selectedMods.put(provided, mod);
            if (prev == null) continue;
            throw new ModResolutionException("duplicate mod %s", provided);
        }
        uniqueSelectedMods.add(mod);
    }
}

