/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.level;

import net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface LevelExtractionContext
extends AbstractLevelRenderContext {
    public ClientLevel level();

    public Camera camera();

    public DeltaTracker deltaTracker();
}

