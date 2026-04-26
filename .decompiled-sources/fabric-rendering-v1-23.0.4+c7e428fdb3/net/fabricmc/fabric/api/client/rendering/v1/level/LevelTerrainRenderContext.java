/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.level;

import net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface LevelTerrainRenderContext
extends AbstractLevelRenderContext {
    public ChunkSectionsToRender sectionsToRender();
}

