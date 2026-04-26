/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Deprecated
public interface SimpleSynchronousResourceReloadListener
extends IdentifiableResourceReloadListener,
ResourceManagerReloadListener {
}

