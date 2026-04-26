/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;

public class KnotClient {
    public static void main(String[] args) {
        Knot.launch(args, EnvType.CLIENT);
    }
}

