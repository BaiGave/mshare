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
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Potion(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<Potion> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default")).forGetter(Potion::defaultColor)).apply((Applicative<Potion, ?>)i, Potion::new));

    public Potion() {
        this(-13083194);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        PotionContents contents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            return ARGB.opaque(contents.getColorOr(this.defaultColor));
        }
        return ARGB.opaque(this.defaultColor);
    }

    public MapCodec<Potion> type() {
        return MAP_CODEC;
    }
}

