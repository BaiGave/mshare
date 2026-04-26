/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment.sync;

import net.minecraft.network.chat.Component;

public class AttachmentSyncException
extends Exception {
    private final Component component;

    public AttachmentSyncException(Component component) {
        super(component.getString());
        this.component = component;
    }

    public Component getComponent() {
        return this.component;
    }
}

