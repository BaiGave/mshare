/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1.pack;

import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.packs.PackResources;

public interface ModPackResources
extends PackResources {
    public ModMetadata getFabricModMetadata();

    public ModPackResources createOverlay(String var1);
}

