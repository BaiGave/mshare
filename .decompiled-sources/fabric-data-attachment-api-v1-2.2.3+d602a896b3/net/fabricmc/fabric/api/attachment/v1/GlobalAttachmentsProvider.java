/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.attachment.v1;

import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;

public interface GlobalAttachmentsProvider {
    default public GlobalAttachments globalAttachments() {
        throw new UnsupportedOperationException("Implemented via mixin!");
    }
}

