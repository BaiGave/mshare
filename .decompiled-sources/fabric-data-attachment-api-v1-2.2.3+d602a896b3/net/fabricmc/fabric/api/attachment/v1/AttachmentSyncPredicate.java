/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.attachment.v1;

import java.util.function.BiPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.NonExtendable
public interface AttachmentSyncPredicate
extends BiPredicate<AttachmentTarget, ServerPlayer> {
    public static AttachmentSyncPredicate all() {
        return (attachmentTarget, serverPlayer) -> true;
    }

    public static AttachmentSyncPredicate targetOnly() {
        return (target, player) -> target == player;
    }

    public static AttachmentSyncPredicate allButTarget() {
        return (target, player) -> target != player;
    }
}

