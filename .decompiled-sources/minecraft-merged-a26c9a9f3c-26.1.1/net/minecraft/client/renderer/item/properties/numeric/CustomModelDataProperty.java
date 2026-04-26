/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record CustomModelDataProperty(int index) implements RangeSelectItemModelProperty
{
    public static final MapCodec<CustomModelDataProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(CustomModelDataProperty::index)).apply((Applicative<CustomModelDataProperty, ?>)i, CustomModelDataProperty::new));

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        Float value;
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData != null && (value = customModelData.getFloat(this.index)) != null) {
            return value.floatValue();
        }
        return 0.0f;
    }

    public MapCodec<CustomModelDataProperty> type() {
        return MAP_CODEC;
    }
}

