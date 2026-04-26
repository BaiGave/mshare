/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public class MutableMeshImpl
extends MeshViewImpl
implements MutableMesh {
    private final MutableQuadViewImpl emitter = new MutableQuadViewImpl(this){
        final /* synthetic */ MutableMeshImpl this$0;
        {
            MutableMeshImpl mutableMeshImpl = this$0;
            Objects.requireNonNull(mutableMeshImpl);
            this.this$0 = mutableMeshImpl;
        }

        @Override
        protected void emitDirectly() {
            this.computeGeometry();
            this.this$0.limit += EncodingFormat.TOTAL_STRIDE;
            this.this$0.ensureCapacity(EncodingFormat.TOTAL_STRIDE);
            this.baseIndex = this.this$0.limit;
        }
    };

    public MutableMeshImpl() {
        this.data = new int[8 * EncodingFormat.TOTAL_STRIDE];
        this.limit = 0;
        this.ensureCapacity(EncodingFormat.TOTAL_STRIDE);
        this.emitter.data = this.data;
        this.emitter.baseIndex = this.limit;
        this.emitter.clear();
    }

    private void ensureCapacity(int stride) {
        if (stride > this.data.length - this.limit) {
            int[] bigger = new int[this.data.length * 2];
            System.arraycopy(this.data, 0, bigger, 0, this.limit);
            this.data = bigger;
            this.emitter.data = this.data;
        }
    }

    @Override
    public QuadEmitter emitter() {
        this.emitter.clear();
        return this.emitter;
    }

    @Override
    public void forEachMutable(Consumer<? super MutableQuadView> action) {
        this.forEach(action, this.emitter);
        this.emitter.data = this.data;
        this.emitter.baseIndex = this.limit;
    }

    @Override
    public Mesh immutableCopy() {
        int[] packed = new int[this.limit];
        System.arraycopy(this.data, 0, packed, 0, this.limit);
        return new MeshImpl(packed);
    }

    @Override
    public void clear() {
        this.emitter.baseIndex = this.limit = 0;
        this.emitter.clear();
    }
}

