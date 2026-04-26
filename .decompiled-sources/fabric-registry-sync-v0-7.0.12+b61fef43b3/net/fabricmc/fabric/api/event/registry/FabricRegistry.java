/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FabricRegistry {
    default public void addAlias(Identifier old, Identifier newId) {
        throw new UnsupportedOperationException("implemented via mixin");
    }
}

