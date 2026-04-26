/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.sound.v1;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;

public interface FabricSoundInstance {
    public static final Identifier EMPTY_SOUND = Identifier.fromNamespaceAndPath("fabric-sound-api-v1", "empty");

    default public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary library, Identifier id, boolean repeatInstantly) {
        return library.getStream(id, repeatInstantly);
    }
}

