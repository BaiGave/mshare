/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public record SingleFile(Identifier resourceId, Optional<Identifier> spriteId) implements SpriteSource
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SingleFile> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Identifier.CODEC.fieldOf("resource")).forGetter(SingleFile::resourceId), Identifier.CODEC.optionalFieldOf("sprite").forGetter(SingleFile::spriteId)).apply((Applicative<SingleFile, ?>)i, SingleFile::new));

    public SingleFile(Identifier resourceId) {
        this(resourceId, Optional.empty());
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Identifier fullResourceId = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> resource = resourceManager.getResource(fullResourceId);
        if (resource.isPresent()) {
            output.add(this.spriteId.orElse(this.resourceId), resource.get());
        } else {
            LOGGER.warn("Missing sprite: {}", (Object)fullResourceId);
        }
    }

    public MapCodec<SingleFile> codec() {
        return MAP_CODEC;
    }
}

