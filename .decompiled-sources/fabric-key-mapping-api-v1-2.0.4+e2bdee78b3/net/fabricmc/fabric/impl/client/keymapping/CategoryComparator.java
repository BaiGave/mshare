/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.keymapping;

import java.util.Comparator;
import net.minecraft.client.KeyMapping;

public class CategoryComparator
implements Comparator<KeyMapping.Category> {
    public static final CategoryComparator INSTANCE = new CategoryComparator();

    @Override
    public int compare(KeyMapping.Category o1, KeyMapping.Category o2) {
        boolean o1Vanilla = o1.id().getNamespace().equals("minecraft");
        boolean o2Vanilla = o2.id().getNamespace().equals("minecraft");
        if (o1Vanilla && o2Vanilla) {
            return 0;
        }
        if (o1Vanilla) {
            return -1;
        }
        if (o2Vanilla) {
            return 1;
        }
        int c = o1.id().getNamespace().compareTo(o2.id().getNamespace());
        if (c != 0) {
            return c;
        }
        return o1.id().getPath().compareTo(o2.id().getPath());
    }
}

