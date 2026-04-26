/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.Nullable;

class ResourceReloaderPhaseData
extends SortableNode<ResourceReloaderPhaseData> {
    final Identifier id;
    PreparableReloadListener resourceReloader;
    VanillaStatus vanillaStatus = VanillaStatus.NONE;

    ResourceReloaderPhaseData(Identifier id, @Nullable PreparableReloadListener resourceReloader) {
        this.id = id;
        this.resourceReloader = resourceReloader;
    }

    void markBefore() {
        boolean isAfter;
        boolean bl = isAfter = this.vanillaStatus == VanillaStatus.AFTER;
        if (this.vanillaStatus != VanillaStatus.NONE && !isAfter) {
            return;
        }
        this.vanillaStatus = VanillaStatus.BEFORE;
        for (ResourceReloaderPhaseData prev : this.previousNodes) {
            prev.markBefore();
        }
    }

    void markAfter() {
        if (this.vanillaStatus != VanillaStatus.NONE) {
            return;
        }
        this.vanillaStatus = VanillaStatus.AFTER;
        for (ResourceReloaderPhaseData next : this.subsequentNodes) {
            next.markAfter();
        }
    }

    void setVanillaStatus(VanillaStatus status) {
        if (this.vanillaStatus == VanillaStatus.NONE) {
            this.vanillaStatus = status;
        }
    }

    @Override
    protected String getDescription() {
        return this.id.toString();
    }

    @Override
    protected void addSubsequentNode(ResourceReloaderPhaseData phase) {
        super.addSubsequentNode(phase);
        if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.AFTER) {
            phase.markAfter();
        }
    }

    @Override
    protected void addPreviousNode(ResourceReloaderPhaseData phase) {
        super.addPreviousNode(phase);
        if (this.vanillaStatus == VanillaStatus.VANILLA || this.vanillaStatus == VanillaStatus.BEFORE) {
            phase.markBefore();
        }
    }

    static enum VanillaStatus {
        NONE,
        AFTER,
        BEFORE,
        VANILLA;

    }

    static class AfterVanilla
    extends ResourceReloaderPhaseData {
        AfterVanilla(Identifier id) {
            super(id, null);
            this.setVanillaStatus(VanillaStatus.VANILLA);
        }

        @Override
        public void markBefore() {
        }

        @Override
        public void markAfter() {
        }
    }
}

