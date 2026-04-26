/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;

public interface CommonPacketHandler {
    public void onCommonVersionPacket(int var1);

    public void onCommonRegisterPacket(CommonRegisterPayload var1);

    public CommonRegisterPayload createRegisterPayload();

    public int getNegotiatedVersion();
}

