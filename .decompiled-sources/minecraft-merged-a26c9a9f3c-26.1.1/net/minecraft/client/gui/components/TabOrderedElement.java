/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface TabOrderedElement {
    default public int getTabOrderGroup() {
        return 0;
    }
}

