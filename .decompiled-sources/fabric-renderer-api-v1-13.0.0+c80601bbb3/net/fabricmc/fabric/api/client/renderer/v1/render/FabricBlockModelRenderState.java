/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import org.joml.Matrix4fc;

public interface FabricBlockModelRenderState {
    default public QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
        throw new IllegalStateException("Implemented via Mixin.");
    }
}

