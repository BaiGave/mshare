/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.model;

import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;

public final class MeshQuadCollection
extends QuadCollection {
    private final Mesh mesh;
    @BakedQuad.MaterialFlags
    private int materialFlags = -1;

    public MeshQuadCollection(Mesh mesh) {
        super(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    @BakedQuad.MaterialFlags
    private static int computeMaterialFlags(Mesh mesh) {
        var quadConsumer = new Consumer<QuadView>(){
            @BakedQuad.MaterialFlags
            int flags = 0;

            @Override
            public void accept(QuadView quad) {
                this.flags |= ModelHelper.computeMaterialFlags(quad);
            }
        };
        mesh.forEach(quadConsumer);
        return quadConsumer.flags;
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        if (this.materialFlags == -1) {
            this.materialFlags = MeshQuadCollection.computeMaterialFlags(this.mesh);
        }
        return this.materialFlags;
    }
}

