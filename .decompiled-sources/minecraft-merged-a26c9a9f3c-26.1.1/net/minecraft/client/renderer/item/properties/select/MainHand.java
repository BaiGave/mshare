/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record MainHand() implements SelectItemModelProperty<HumanoidArm>
{
    public static final Codec<HumanoidArm> VALUE_CODEC = HumanoidArm.CODEC;
    public static final SelectItemModelProperty.Type<MainHand, HumanoidArm> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(new MainHand()), VALUE_CODEC);

    @Override
    public @Nullable HumanoidArm get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return owner == null ? null : owner.getMainArm();
    }

    @Override
    public SelectItemModelProperty.Type<MainHand, HumanoidArm> type() {
        return TYPE;
    }

    @Override
    public Codec<HumanoidArm> valueCodec() {
        return VALUE_CODEC;
    }
}

