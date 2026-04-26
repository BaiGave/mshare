/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1.context;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface PacketContextProvider {
    default public PacketContext getPacketContext() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

