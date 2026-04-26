/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import org.jetbrains.annotations.Range;

public class MeshViewImpl
implements MeshView {
    private static final ThreadLocal<ObjectArrayList<QuadViewImpl>> CURSOR_POOLS = ThreadLocal.withInitial(ObjectArrayList::new);
    int[] data;
    int limit;

    MeshViewImpl() {
    }

    @Override
    public @Range(from=0L, to=0x7FFFFFFFL) int size() {
        return this.limit / EncodingFormat.TOTAL_STRIDE;
    }

    @Override
    public void forEach(Consumer<? super QuadView> action) {
        ObjectArrayList<QuadViewImpl> pool = CURSOR_POOLS.get();
        QuadViewImpl cursor = pool.isEmpty() ? new QuadViewImpl() : (QuadViewImpl)pool.pop();
        this.forEach(action, cursor);
        pool.push(cursor);
    }

    <C extends QuadViewImpl> void forEach(Consumer<? super C> action, C cursor) {
        int limit = this.limit;
        cursor.data = this.data;
        for (int index = 0; index < limit; index += EncodingFormat.TOTAL_STRIDE) {
            cursor.baseIndex = index;
            cursor.load();
            action.accept(cursor);
        }
        cursor.data = null;
    }

    @Override
    public void outputTo(QuadEmitter emitter) {
        MutableQuadViewImpl e = (MutableQuadViewImpl)emitter;
        int[] data = this.data;
        int limit = this.limit;
        for (int index = 0; index < limit; index += EncodingFormat.TOTAL_STRIDE) {
            System.arraycopy(data, index, e.data, e.baseIndex, EncodingFormat.TOTAL_STRIDE);
            e.load();
            e.transformAndEmit();
        }
        e.clear();
    }
}

