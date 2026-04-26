/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import java.io.Serializable;
import java.util.Comparator;
import net.fabricmc.loader.impl.lib.sat4j.specs.IteratorInt;

public interface IVecInt
extends Serializable,
Cloneable {
    public int size();

    public void shrink(int var1);

    public IVecInt pop();

    public void growTo(int var1, int var2);

    public void ensure(int var1);

    public IVecInt push(int var1);

    public void unsafePush(int var1);

    public int unsafeGet(int var1);

    public void clear();

    public int last();

    public int get(int var1);

    public void set(int var1, int var2);

    public boolean contains(int var1);

    public int indexOf(int var1);

    public void copyTo(IVecInt var1);

    public void copyTo(int[] var1);

    public void moveTo(int[] var1);

    public void moveTo(int var1, int var2);

    public void remove(int var1);

    public int delete(int var1);

    public void sort();

    public void sort(Comparator<Integer> var1);

    public void sortUnique();

    public boolean isEmpty();

    public IteratorInt iterator();

    public int[] toArray();
}

