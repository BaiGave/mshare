/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.LightCoordsUtil;

public record Brightness(int block, int sky) {
    public static final Codec<Integer> LIGHT_VALUE_CODEC = ExtraCodecs.intRange(0, 15);
    public static final Codec<Brightness> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)LIGHT_VALUE_CODEC.fieldOf("block")).forGetter(Brightness::block), ((MapCodec)LIGHT_VALUE_CODEC.fieldOf("sky")).forGetter(Brightness::sky)).apply((Applicative<Brightness, ?>)i, Brightness::new));
    public static final Brightness FULL_BRIGHT = new Brightness(15, 15);

    public int pack() {
        return LightCoordsUtil.pack(this.block, this.sky);
    }

    public static Brightness unpack(int packed) {
        return new Brightness(LightCoordsUtil.block(packed), LightCoordsUtil.sky(packed));
    }
}

