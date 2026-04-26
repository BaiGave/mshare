/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.pb;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import net.fabricmc.loader.impl.lib.sat4j.pb.ObjectiveFunction;

public class ObjectiveFunctionComparator
implements Serializable,
Comparator<Integer> {
    private final Map<Integer, BigInteger> obj;

    public ObjectiveFunctionComparator(ObjectiveFunction objf) {
        this.obj = objf.toMap();
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        BigInteger b1 = this.obj.get(o1);
        BigInteger b2 = this.obj.get(o2);
        if (b2 == null) {
            if (b1 == null) {
                return 0;
            }
            return -b1.intValue();
        }
        return b2.compareTo(b1);
    }
}

