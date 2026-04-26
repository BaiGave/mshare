/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

public interface MutableMesh
extends MeshView {
    public QuadEmitter emitter();

    public void forEachMutable(Consumer<? super MutableQuadView> var1);

    public Mesh immutableCopy();

    public void clear();
}

