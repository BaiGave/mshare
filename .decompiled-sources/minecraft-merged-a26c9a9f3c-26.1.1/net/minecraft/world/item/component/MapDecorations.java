/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.util.Util;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public record MapDecorations(Map<String, Entry> decorations) {
    public static final MapDecorations EMPTY = new MapDecorations(Map.of());
    public static final Codec<MapDecorations> CODEC = Codec.unboundedMap(Codec.STRING, Entry.CODEC).xmap(MapDecorations::new, MapDecorations::decorations);

    public MapDecorations withDecoration(String id, Entry entry) {
        return new MapDecorations(Util.copyAndPut(this.decorations, id, entry));
    }

    public record Entry(Holder<MapDecorationType> type, double x, double z, float rotation) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)MapDecorationType.CODEC.fieldOf("type")).forGetter(Entry::type), ((MapCodec)Codec.DOUBLE.fieldOf("x")).forGetter(Entry::x), ((MapCodec)Codec.DOUBLE.fieldOf("z")).forGetter(Entry::z), ((MapCodec)Codec.FLOAT.fieldOf("rotation")).forGetter(Entry::rotation)).apply((Applicative<Entry, ?>)i, Entry::new));
    }
}

