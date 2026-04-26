/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record HasComponent(DataComponentType<?> componentType, boolean ignoreDefault) implements ConditionalItemModelProperty
{
    public static final MapCodec<HasComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("component")).forGetter(HasComponent::componentType), Codec.BOOL.optionalFieldOf("ignore_default", false).forGetter(HasComponent::ignoreDefault)).apply((Applicative<HasComponent, ?>)i, HasComponent::new));

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return this.ignoreDefault ? itemStack.hasNonDefault(this.componentType) : itemStack.has(this.componentType);
    }

    public MapCodec<HasComponent> type() {
        return MAP_CODEC;
    }
}

