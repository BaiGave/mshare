/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.saveddata.maps;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record MapFrame(BlockPos pos, int rotation, int entityId) {
    public static final Codec<MapFrame> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockPos.CODEC.fieldOf("pos")).forGetter(MapFrame::pos), ((MapCodec)Codec.INT.fieldOf("rotation")).forGetter(MapFrame::rotation), ((MapCodec)Codec.INT.fieldOf("entity_id")).forGetter(MapFrame::entityId)).apply((Applicative<MapFrame, ?>)i, MapFrame::new));

    public String getId() {
        return MapFrame.frameId(this.pos);
    }

    public static String frameId(BlockPos pos) {
        return "frame-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}

