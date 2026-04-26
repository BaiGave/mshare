/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapItemColor;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record MapColor(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<MapColor> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default")).forGetter(MapColor::defaultColor)).apply((Applicative<MapColor, ?>)i, MapColor::new));

    public MapColor() {
        this(MapItemColor.DEFAULT.rgb());
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        MapItemColor component = itemStack.get(DataComponents.MAP_COLOR);
        if (component != null) {
            return ARGB.opaque(component.rgb());
        }
        return ARGB.opaque(this.defaultColor);
    }

    public MapCodec<MapColor> type() {
        return MAP_CODEC;
    }
}

