/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshViewImpl;

public class MeshImpl
extends MeshViewImpl
implements Mesh {
    public MeshImpl(int[] data) {
        this.data = data;
        this.limit = data.length;
    }
}

