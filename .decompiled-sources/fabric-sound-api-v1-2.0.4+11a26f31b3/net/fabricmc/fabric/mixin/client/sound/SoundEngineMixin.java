/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.sound;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={SoundEngine.class})
public class SoundEngineMixin {
    @Redirect(method={"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<?> getStream(SoundBufferLibrary library, Identifier id, boolean looping, SoundInstance sound) {
        return sound.getAudioStream(library, id, looping);
    }
}

