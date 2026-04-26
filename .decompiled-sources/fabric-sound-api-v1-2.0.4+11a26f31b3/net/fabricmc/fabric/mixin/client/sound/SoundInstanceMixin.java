/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.sound;

import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={SoundInstance.class})
public interface SoundInstanceMixin
extends FabricSoundInstance {
}

