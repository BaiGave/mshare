/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.debug.client.renderer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererFactory;
import net.minecraft.util.debug.DebugSubscription;

public final class DebugRendererRegistryImpl {
    public static final Set<Entry> RENDERERS = new HashSet<Entry>();

    public static <T> void register(DebugSubscription<T> debugSubscription, DebugRendererFactory rendererFactory) {
        RENDERERS.add(new Entry(debugSubscription, rendererFactory));
    }

    public record Entry(DebugSubscription<?> debugSubscription, DebugRendererFactory rendererFactory) {
        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry)o;
            return Objects.equals(this.debugSubscription, entry.debugSubscription);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.debugSubscription);
        }
    }
}

