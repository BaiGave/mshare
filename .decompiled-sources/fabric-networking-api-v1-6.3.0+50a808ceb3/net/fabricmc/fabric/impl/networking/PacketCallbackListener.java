/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.protocol.Packet;

public interface PacketCallbackListener {
    public void sent(Packet<?> var1);
}

