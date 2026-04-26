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
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Constant(int value) implements ItemTintSource
{
    public static final MapCodec<Constant> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("value")).forGetter(Constant::value)).apply((Applicative<Constant, ?>)i, Constant::new));

    public Constant(int value) {
        this.value = value = ARGB.opaque(value);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return this.value;
    }

    public MapCodec<Constant> type() {
        return MAP_CODEC;
    }
}

