/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentEntrypoint
implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");

    @Override
    public void onInitialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> AttachmentTargetImpl.transfer(oldPlayer, newPlayer, !alive));
        ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register((originalEntity, newEntity, origin, destination) -> AttachmentTargetImpl.transfer(originalEntity, newEntity, false));
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, keepEquipment) -> AttachmentTargetImpl.transfer(previous, converted, true));
    }
}

