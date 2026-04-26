/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SoundInstance;

@Environment(value=EnvType.CLIENT)
public interface TickableSoundInstance
extends SoundInstance {
    public boolean isStopped();

    public void tick();
}

