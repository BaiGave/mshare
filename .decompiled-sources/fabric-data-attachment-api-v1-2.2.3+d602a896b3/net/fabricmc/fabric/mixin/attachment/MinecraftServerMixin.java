/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachmentsProvider;
import net.fabricmc.fabric.impl.attachment.AttachmentSavedData;
import net.fabricmc.fabric.impl.attachment.GlobalAttachmentsImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftServer.class})
public abstract class MinecraftServerMixin
implements GlobalAttachmentsProvider {
    @Shadow
    @Final
    private SavedDataStorage savedDataStorage;
    @Unique
    private GlobalAttachmentsImpl globalAttachments;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void initGlobalAttachments(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer)((Object)this);
        this.globalAttachments = new GlobalAttachmentsImpl(server);
        SavedDataType<AttachmentSavedData> type = new SavedDataType<AttachmentSavedData>(AttachmentSavedData.ID, () -> new AttachmentSavedData(this.globalAttachments), AttachmentSavedData.codec(server), null);
        this.savedDataStorage.computeIfAbsent(type);
    }

    @Override
    public GlobalAttachments globalAttachments() {
        return this.globalAttachments;
    }
}

