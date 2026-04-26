/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ItemBlockState(String property) implements SelectItemModelProperty<String>
{
    public static final PrimitiveCodec<String> VALUE_CODEC = Codec.STRING;
    public static final SelectItemModelProperty.Type<ItemBlockState, String> TYPE = SelectItemModelProperty.Type.create(RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.STRING.fieldOf("block_state_property")).forGetter(ItemBlockState::property)).apply((Applicative<ItemBlockState, ?>)i, ItemBlockState::new)), VALUE_CODEC);

    @Override
    public @Nullable String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        BlockItemStateProperties blockItemStateProperties = itemStack.get(DataComponents.BLOCK_STATE);
        if (blockItemStateProperties == null) {
            return null;
        }
        return blockItemStateProperties.properties().get(this.property);
    }

    @Override
    public SelectItemModelProperty.Type<ItemBlockState, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }
}

