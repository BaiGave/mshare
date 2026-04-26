/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;

public class ModPackResourcesSorter {
    private final Object lock = new Object();
    private ModPackResources[] packs;
    private final Map<String, LoadPhaseData> phases = new LinkedHashMap<String, LoadPhaseData>();
    private final List<LoadPhaseData> sortedPhases = new ArrayList<LoadPhaseData>();

    ModPackResourcesSorter() {
        this.packs = new ModPackResources[0];
    }

    public List<ModPackResources> getPacks() {
        return Collections.unmodifiableList(Arrays.asList(this.packs));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addPack(ModPackResources pack) {
        Objects.requireNonNull(pack, "Can't register a null pack");
        String modId = pack.packId();
        Objects.requireNonNull(modId, "Can't register a pack without a mod id");
        Object object = this.lock;
        synchronized (object) {
            this.getOrCreatePhase(modId, true).addPack(pack);
            this.rebuildPackList(this.packs.length + 1);
        }
    }

    private LoadPhaseData getOrCreatePhase(String id, boolean sortIfCreate) {
        LoadPhaseData phase = this.phases.get(id);
        if (phase == null) {
            phase = new LoadPhaseData(id);
            this.phases.put(id, phase);
            this.sortedPhases.add(phase);
            if (sortIfCreate) {
                NodeSorting.sort(this.sortedPhases, "mod resource packs", Comparator.comparing(data -> data.modId));
            }
        }
        return phase;
    }

    private void rebuildPackList(int newLength) {
        if (this.sortedPhases.size() == 1) {
            this.packs = this.sortedPhases.getFirst().packs;
        } else {
            ModPackResources[] newHandlers = new ModPackResources[newLength];
            int newHandlersIndex = 0;
            for (LoadPhaseData existingPhase : this.sortedPhases) {
                int length = existingPhase.packs.length;
                System.arraycopy(existingPhase.packs, 0, newHandlers, newHandlersIndex, length);
                newHandlersIndex += length;
            }
            this.packs = newHandlers;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addLoadOrdering(String firstPhase, String secondPhase, ModPackResourcesUtil.Order order) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase)) {
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
        }
        Object object = this.lock;
        synchronized (object) {
            LoadPhaseData first = this.getOrCreatePhase(firstPhase, false);
            LoadPhaseData second = this.getOrCreatePhase(secondPhase, false);
            switch (order) {
                case BEFORE: {
                    LoadPhaseData.link(first, second);
                    break;
                }
                case AFTER: {
                    LoadPhaseData.link(second, first);
                }
            }
            NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> data.modId));
            this.rebuildPackList(this.packs.length);
        }
    }

    public static class LoadPhaseData
    extends SortableNode<LoadPhaseData> {
        final String modId;
        ModPackResources[] packs;

        LoadPhaseData(String modId) {
            this.modId = modId;
            this.packs = new ModPackResources[0];
        }

        void addPack(ModPackResources pack) {
            int oldLength = this.packs.length;
            this.packs = Arrays.copyOf(this.packs, oldLength + 1);
            this.packs[oldLength] = pack;
        }

        @Override
        protected String getDescription() {
            return this.modId;
        }
    }
}

