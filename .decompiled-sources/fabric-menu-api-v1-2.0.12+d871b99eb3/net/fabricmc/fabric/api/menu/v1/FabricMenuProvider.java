/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.menu.v1;

public interface FabricMenuProvider {
    default public boolean shouldCloseCurrentScreen() {
        return true;
    }
}

