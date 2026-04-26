/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;

final class ModPrioSorter {
    private static final Comparator<ModCandidateImpl> comparator = new Comparator<ModCandidateImpl>(){

        @Override
        public int compare(ModCandidateImpl a, ModCandidateImpl b) {
            return ModPrioSorter.compare(a, b);
        }
    };

    ModPrioSorter() {
    }

    static void sort(List<ModCandidateImpl> mods, Map<String, List<ModCandidateImpl>> modsById) {
        mods.sort(comparator);
        HashSet<String> providedMods = new HashSet<String>();
        for (ModCandidateImpl mod : mods) {
            modsById.computeIfAbsent(mod.getId(), ignore -> new ArrayList()).add(mod);
            for (String provided : mod.getProvides()) {
                modsById.computeIfAbsent(provided, ignore -> new ArrayList()).add(mod);
                providedMods.add(provided);
            }
        }
        Iterator it = providedMods.iterator();
        while (it.hasNext()) {
            if (modsById.get(it.next()).size() > 1) continue;
            it.remove();
        }
        if (providedMods.isEmpty()) {
            return;
        }
        boolean movedPastRoots = false;
        int startIdx = 0;
        HashSet<String> potentiallyOverlappingIds = new HashSet<String>();
        int size = mods.size();
        for (int i = 0; i < size; ++i) {
            ModCandidateImpl cmpMod;
            String cmpId;
            ModCandidateImpl mod = mods.get(i);
            String id = mod.getId();
            if (!movedPastRoots && !mod.isRoot()) {
                movedPastRoots = true;
                startIdx = i;
            }
            if (providedMods.contains(id)) {
                potentiallyOverlappingIds.add(id);
            }
            if (!mod.getProvides().isEmpty()) {
                for (String provId : mod.getProvides()) {
                    if (!providedMods.contains(provId)) continue;
                    potentiallyOverlappingIds.add(provId);
                }
            }
            if (potentiallyOverlappingIds.isEmpty()) continue;
            int earliestIdx = -1;
            for (int j = i - 1; j >= startIdx && !(cmpId = (cmpMod = mods.get(j)).getId()).equals(id); --j) {
                if (!potentiallyOverlappingIds.contains(cmpId) && (cmpMod.getProvides().isEmpty() || Collections.disjoint(potentiallyOverlappingIds, cmpMod.getProvides()))) continue;
                int cmp = ModPrioSorter.compareOverlappingIds(mod, cmpMod, Integer.MAX_VALUE);
                if (cmp < 0) {
                    earliestIdx = j;
                    continue;
                }
                if (cmp != Integer.MAX_VALUE) break;
            }
            if (earliestIdx >= 0) {
                mods.remove(i);
                mods.add(earliestIdx, mod);
            }
            potentiallyOverlappingIds.clear();
        }
    }

    private static int compare(ModCandidateImpl a, ModCandidateImpl b) {
        int idCmp;
        if (a.isRoot()) {
            if (!b.isRoot()) {
                return -1;
            }
        } else if (b.isRoot()) {
            return 1;
        }
        if ((idCmp = a.getId().compareTo(b.getId())) != 0) {
            return idCmp;
        }
        int versionCmp = b.getVersion().compareTo(a.getVersion());
        if (versionCmp != 0) {
            return versionCmp;
        }
        int nestCmp = a.getMinNestLevel() - b.getMinNestLevel();
        if (nestCmp != 0) {
            return nestCmp;
        }
        if (a.isRoot()) {
            return 0;
        }
        return ModPrioSorter.compareParents(a, b);
    }

    private static int compareParents(ModCandidateImpl a, ModCandidateImpl b) {
        assert (!a.getParentMods().isEmpty() && !b.getParentMods().isEmpty());
        ModCandidateImpl minParent = null;
        for (ModCandidateImpl mod : a.getParentMods()) {
            if (minParent != null && (mod == minParent || ModPrioSorter.compare(minParent, mod) <= 0)) continue;
            minParent = mod;
        }
        assert (minParent != null);
        boolean found = false;
        for (ModCandidateImpl mod : b.getParentMods()) {
            if (mod == minParent) {
                found = true;
                continue;
            }
            if (ModPrioSorter.compare(minParent, mod) <= 0) continue;
            return 1;
        }
        return found ? 0 : -1;
    }

    private static int compareOverlappingIds(ModCandidateImpl a, ModCandidateImpl b, int noMatchResult) {
        assert (!a.getId().equals(b.getId()));
        int ret = 0;
        boolean matched = false;
        for (String provIdA : a.getProvides()) {
            if (!provIdA.equals(b.getId())) continue;
            Version providedVersionA = a.getVersion();
            ret += Integer.signum(b.getVersion().compareTo(providedVersionA));
            matched = true;
        }
        block1: for (String provIdB : b.getProvides()) {
            if (provIdB.equals(a.getId())) {
                Version providedVersionB = b.getVersion();
                ret += Integer.signum(providedVersionB.compareTo(a.getVersion()));
                matched = true;
                continue;
            }
            for (String provIdA : a.getProvides()) {
                if (!provIdB.equals(provIdA)) continue;
                Version providedVersionA = a.getVersion();
                Version providedVersionB = b.getVersion();
                ret += Integer.signum(providedVersionB.compareTo(providedVersionA));
                matched = true;
                continue block1;
            }
        }
        return matched ? ret : noMatchResult;
    }
}

