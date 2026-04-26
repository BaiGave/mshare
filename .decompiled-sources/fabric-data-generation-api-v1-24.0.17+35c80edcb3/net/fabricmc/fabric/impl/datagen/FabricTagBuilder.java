/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen;

import net.minecraft.resources.Identifier;

public interface FabricTagBuilder {
    public void fabric_setReplace(boolean var1);

    public boolean fabric_isReplaced();

    public void fabric_forceAddTag(Identifier var1);
}

