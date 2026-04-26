/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Deprecated
public interface ModResourcePack
extends ModPackResources {
    @Override
    public ModMetadata getFabricModMetadata();

    @Override
    public ModResourcePack createOverlay(String var1);
}

