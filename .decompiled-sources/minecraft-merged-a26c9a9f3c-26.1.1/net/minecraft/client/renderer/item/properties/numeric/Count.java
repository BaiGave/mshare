/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Count(boolean normalize) implements RangeSelectItemModelProperty
{
    public static final MapCodec<Count> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("normalize", true).forGetter(Count::normalize)).apply((Applicative<Count, ?>)i, Count::new));

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        float count = itemStack.getCount();
        float maxCount = itemStack.getMaxStackSize();
        if (this.normalize) {
            return Mth.clamp(count / maxCount, 0.0f, 1.0f);
        }
        return Mth.clamp(count, 0.0f, maxCount);
    }

    public MapCodec<Count> type() {
        return MAP_CODEC;
    }
}

