/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.registry.sync.RemovableIdMapper;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={IdMapper.class})
public class IdMapperMixin<T>
implements RemovableIdMapper<T> {
    @Shadow
    private int nextId;
    @Final
    @Shadow
    private Reference2IntMap<T> tToId;
    @Final
    @Shadow
    private List<T> idToT;

    @Override
    public void fabric_clear() {
        this.nextId = 0;
        this.tToId.clear();
        this.idToT.clear();
    }

    @Unique
    private void fabric_removeInner(T o) {
        int value = this.tToId.removeInt(o);
        this.idToT.set(value, null);
        while (this.nextId > 1 && this.idToT.get(this.nextId - 1) == null) {
            --this.nextId;
        }
    }

    @Override
    public void fabric_remove(T o) {
        if (this.tToId.containsKey(o)) {
            this.fabric_removeInner(o);
        }
    }

    @Override
    public void fabric_removeId(int i) {
        ArrayList removals = new ArrayList();
        for (Object o : this.tToId.keySet()) {
            int j = this.tToId.getInt(o);
            if (i != j) continue;
            removals.add(o);
        }
        removals.forEach(this::fabric_removeInner);
    }

    @Override
    public void fabric_remapId(int from, int to) {
        this.fabric_remapIds(Int2IntMaps.singleton(from, to));
    }

    @Override
    public void fabric_remapIds(Int2IntMap map) {
        this.tToId.replaceAll((a, b) -> map.get((int)b));
        this.nextId = 0;
        ArrayList<T> oldList = new ArrayList<T>(this.idToT);
        this.idToT.clear();
        for (int k = 0; k < oldList.size(); ++k) {
            Object o = oldList.get(k);
            if (o == null) continue;
            int i = map.getOrDefault(k, k);
            while (this.idToT.size() <= i) {
                this.idToT.add(null);
            }
            this.idToT.set(i, o);
            if (this.nextId > i) continue;
            this.nextId = i + 1;
        }
    }
}

