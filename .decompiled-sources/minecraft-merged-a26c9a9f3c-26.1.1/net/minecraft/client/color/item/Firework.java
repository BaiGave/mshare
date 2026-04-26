/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Firework(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<Firework> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default")).forGetter(Firework::defaultColor)).apply((Applicative<Firework, ?>)i, Firework::new));

    public Firework() {
        this(-7697782);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        FireworkExplosion explosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
        IntList explosionColors = explosion != null ? explosion.colors() : IntList.of();
        int colorCount = explosionColors.size();
        if (colorCount == 0) {
            return this.defaultColor;
        }
        if (colorCount == 1) {
            return ARGB.opaque(explosionColors.getInt(0));
        }
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        for (int i = 0; i < colorCount; ++i) {
            int color = explosionColors.getInt(i);
            totalRed += ARGB.red(color);
            totalGreen += ARGB.green(color);
            totalBlue += ARGB.blue(color);
        }
        return ARGB.color(totalRed / colorCount, totalGreen / colorCount, totalBlue / colorCount);
    }

    public MapCodec<Firework> type() {
        return MAP_CODEC;
    }
}

