/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record CustomModelDataProperty(int index) implements SelectItemModelProperty<String>
{
    public static final PrimitiveCodec<String> VALUE_CODEC = Codec.STRING;
    public static final SelectItemModelProperty.Type<CustomModelDataProperty, String> TYPE = SelectItemModelProperty.Type.create(RecordCodecBuilder.mapCodec(i -> i.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(CustomModelDataProperty::index)).apply((Applicative<CustomModelDataProperty, ?>)i, CustomModelDataProperty::new)), VALUE_CODEC);

    @Override
    public @Nullable String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData != null) {
            return customModelData.getString(this.index);
        }
        return null;
    }

    @Override
    public SelectItemModelProperty.Type<CustomModelDataProperty, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }
}

