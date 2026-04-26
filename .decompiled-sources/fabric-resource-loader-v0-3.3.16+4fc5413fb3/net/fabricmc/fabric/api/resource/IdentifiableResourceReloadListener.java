/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

@Deprecated
public interface IdentifiableResourceReloadListener
extends PreparableReloadListener {
    public Identifier getFabricId();

    default public Collection<Identifier> getFabricDependencies() {
        return Collections.emptyList();
    }
}

