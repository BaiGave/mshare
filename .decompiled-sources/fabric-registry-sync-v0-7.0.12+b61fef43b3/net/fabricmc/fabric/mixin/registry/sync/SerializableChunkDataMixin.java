/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={SerializableChunkData.class})
public class SerializableChunkDataMixin {
    @Redirect(method={"lambda$unpackStructureReferences$0"}, at=@At(value="INVOKE", target="Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void log(Logger logger, String msg, Object identifier, Object chunkPos) {
        logger.debug(msg, identifier, chunkPos);
    }
}

