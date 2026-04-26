/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeLayerSettings {
    private static final Codec<Double> LAYER_RANGE = Codec.doubleRange(0.01, 50.0);
    public static final Codec<GeodeLayerSettings> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)LAYER_RANGE.fieldOf("filling")).orElse(1.7).forGetter(c -> c.filling), ((MapCodec)LAYER_RANGE.fieldOf("inner_layer")).orElse(2.2).forGetter(c -> c.innerLayer), ((MapCodec)LAYER_RANGE.fieldOf("middle_layer")).orElse(3.2).forGetter(c -> c.middleLayer), ((MapCodec)LAYER_RANGE.fieldOf("outer_layer")).orElse(4.2).forGetter(c -> c.outerLayer)).apply((Applicative<GeodeLayerSettings, ?>)i, GeodeLayerSettings::new));
    public final double filling;
    public final double innerLayer;
    public final double middleLayer;
    public final double outerLayer;

    public GeodeLayerSettings(double filling, double innerLayer, double middleLayer, double outerLayer) {
        this.filling = filling;
        this.innerLayer = innerLayer;
        this.middleLayer = middleLayer;
        this.outerLayer = outerLayer;
    }
}

