/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class RemapException
extends Exception {
    private final @Nullable Component component;

    public RemapException(String message) {
        super(message);
        this.component = null;
    }

    public RemapException(Component component) {
        super(component.getString());
        this.component = component;
    }

    public @Nullable Component getComponent() {
        return this.component;
    }
}

