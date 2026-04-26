/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record IsUsingItem() implements ConditionalItemModelProperty
{
    public static final MapCodec<IsUsingItem> MAP_CODEC = MapCodec.unit(new IsUsingItem());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        if (owner == null) {
            return false;
        }
        return owner.isUsingItem() && owner.getUseItem() == itemStack;
    }

    public MapCodec<IsUsingItem> type() {
        return MAP_CODEC;
    }
}

