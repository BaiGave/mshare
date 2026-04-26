/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.GlobalAttachmentsProvider;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={Level.class})
abstract class LevelMixin
implements AttachmentTargetImpl,
GlobalAttachmentsProvider {
    LevelMixin() {
    }

    @Shadow
    public abstract boolean isClientSide();

    @Shadow
    public abstract RegistryAccess registryAccess();

    @Override
    public boolean fabric_shouldTryToSync() {
        return !this.isClientSide();
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return this.registryAccess();
    }
}

