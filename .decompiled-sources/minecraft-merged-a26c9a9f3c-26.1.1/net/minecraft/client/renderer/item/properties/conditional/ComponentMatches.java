/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ComponentMatches(DataComponentPredicate.Single<?> predicate) implements ConditionalItemModelProperty
{
    public static final MapCodec<ComponentMatches> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(DataComponentPredicate.singleCodec("predicate").forGetter(ComponentMatches::predicate)).apply((Applicative<ComponentMatches, ?>)i, ComponentMatches::new));

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return this.predicate.predicate().matches(itemStack);
    }

    public MapCodec<ComponentMatches> type() {
        return MAP_CODEC;
    }
}

