/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.fabricmc.fabric.mixin.transfer.ItemStackAccessor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class VariantCodecs {
    private static final Codec<ItemVariant> UNVALIDATED_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item")).forGetter(ItemVariant::typeHolder), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(TransferVariant::getComponentsPatch)).apply((Applicative<ItemVariant, ?>)instance, ItemVariantImpl::of));
    public static final Codec<ItemVariant> ITEM_CODEC = UNVALIDATED_ITEM_CODEC.validate(VariantCodecs::validateComponents);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemVariant> ITEM_PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.ITEM), ItemVariant::typeHolder, DataComponentPatch.STREAM_CODEC, TransferVariant::getComponentsPatch, ItemVariantImpl::of);
    public static final Codec<FluidVariant> FLUID_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid")).forGetter(FluidVariant::typeHolder), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(TransferVariant::getComponentsPatch)).apply((Applicative<FluidVariant, ?>)instance, FluidVariantImpl::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidVariant> FLUID_PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.FLUID), FluidVariant::typeHolder, DataComponentPatch.STREAM_CODEC, TransferVariant::getComponentsPatch, FluidVariantImpl::of);

    private static DataResult<ItemVariant> validateComponents(ItemVariant variant) {
        return ItemStackAccessor.validateComponents(PatchedDataComponentMap.fromPatch(variant.getItem().components(), variant.getComponentsPatch())).map(v -> variant);
    }
}

