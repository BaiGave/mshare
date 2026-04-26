/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.Comparator;
import net.minecraft.resources.Identifier;

public class DebugOptionsComparator
implements Comparator<Identifier> {
    public static final DebugOptionsComparator INSTANCE = new DebugOptionsComparator();

    @Override
    public int compare(Identifier o1, Identifier o2) {
        boolean o1IsMinecraft = "minecraft".equals(o1.getNamespace());
        boolean o2IsMinecraft = "minecraft".equals(o2.getNamespace());
        if (o1IsMinecraft && !o2IsMinecraft) {
            return -1;
        }
        if (!o1IsMinecraft && o2IsMinecraft) {
            return 1;
        }
        int c = o1.getNamespace().compareTo(o2.getNamespace());
        if (c != 0) {
            return c;
        }
        return o1.getPath().compareTo(o2.getPath());
    }
}

