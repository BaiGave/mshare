/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.attachment;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentSerializingImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.world.level.storage.ValueInput;

public class DataAccessorHandler {
    public static final ScopedValue<Void> APPLYING_DATA_CHANGE = ScopedValue.newInstance();

    public static void applyDataChanges(AttachmentTarget target, ValueInput data, Runnable applyData) {
        IdentityHashMap<AttachmentType<?>, Object> newAttachments;
        Map<AttachmentType<?>, ?> oldAttachments;
        block6: {
            block7: {
                AttachmentTargetImpl targetImpl = (AttachmentTargetImpl)target;
                oldAttachments = targetImpl.fabric_getAttachments();
                ScopedValue.where(APPLYING_DATA_CHANGE, null).run(applyData);
                if (oldAttachments != targetImpl.fabric_getAttachments()) {
                    throw new AssertionError((Object)"Attachment data changed during data change application.");
                }
                newAttachments = AttachmentSerializingImpl.deserializeAttachmentData(data);
                if (oldAttachments == null && newAttachments == null) {
                    return;
                }
                if (oldAttachments == null) break block6;
                if (newAttachments == null) break block7;
                if (!newAttachments.isEmpty()) break block6;
            }
            oldAttachments.keySet().stream().filter(AttachmentType::isPersistent).toList().forEach(target::removeAttached);
            return;
        }
        newAttachments.forEach((attachmentType, o) -> target.setAttached(attachmentType, o));
        if (oldAttachments != null) {
            oldAttachments.keySet().stream().filter(AttachmentType::isPersistent).filter(attachmentType -> !newAttachments.containsKey(attachmentType)).toList().forEach(target::removeAttached);
        }
    }
}

