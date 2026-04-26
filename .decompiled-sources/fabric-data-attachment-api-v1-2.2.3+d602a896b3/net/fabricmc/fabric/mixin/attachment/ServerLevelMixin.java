/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentSavedData;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerLevel.class})
abstract class ServerLevelMixin
extends Level
implements AttachmentTargetImpl {
    @Shadow
    @Final
    private MinecraftServer server;

    protected ServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(at={@At(value="TAIL")}, method={"<init>"})
    private void createAttachmentsPersistentState(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)((Object)this);
        SavedDataType<AttachmentSavedData> type = new SavedDataType<AttachmentSavedData>(AttachmentSavedData.ID, () -> new AttachmentSavedData(level), AttachmentSavedData.codec(level), null);
        level.getDataStorage().computeIfAbsent(type);
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        ServerLevelMixin serverLevelMixin = this;
        if (serverLevelMixin instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)((Object)serverLevelMixin);
            PlayerLookup.level(serverLevel).forEach(player -> {
                if (((AttachmentTypeImpl)type).syncPredicate().test(this, player)) {
                    AttachmentSync.trySync(change, player);
                }
            });
        }
    }

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return AttachmentTargetInfo.LevelTarget.INSTANCE;
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return this.registryAccess();
    }

    @Override
    public GlobalAttachments globalAttachments() {
        return this.server.globalAttachments();
    }
}

