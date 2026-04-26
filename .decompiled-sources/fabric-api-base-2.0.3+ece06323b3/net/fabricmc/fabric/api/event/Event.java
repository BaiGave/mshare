/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public abstract class Event<T> {
    protected volatile T invoker;
    public static final Identifier DEFAULT_PHASE = Identifier.fromNamespaceAndPath("fabric", "default");

    public final T invoker() {
        return this.invoker;
    }

    public abstract void register(T var1);

    public void register(Identifier phase, T listener) {
        this.register(listener);
    }

    public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
    }
}

