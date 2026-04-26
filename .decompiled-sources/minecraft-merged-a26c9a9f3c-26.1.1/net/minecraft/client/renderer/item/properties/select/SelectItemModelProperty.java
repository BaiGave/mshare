/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.select;

import com.google.common.collect.HashMultiset;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SelectItemModelProperty<T> {
    public @Nullable T get(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4, ItemDisplayContext var5);

    public Codec<T> valueCodec();

    public Type<? extends SelectItemModelProperty<T>, T> type();

    @Environment(value=EnvType.CLIENT)
    public record Type<P extends SelectItemModelProperty<T>, T>(MapCodec<SelectItemModel.UnbakedSwitch<P, T>> switchCodec) {
        public static <P extends SelectItemModelProperty<T>, T> Type<P, T> create(MapCodec<P> propertyMapCodec, Codec<T> valueCodec) {
            MapCodec<SelectItemModel.UnbakedSwitch<P, T>> switchCodec = RecordCodecBuilder.mapCodec(i -> i.group(propertyMapCodec.forGetter(SelectItemModel.UnbakedSwitch::property), Type.createCasesFieldCodec(valueCodec).forGetter(SelectItemModel.UnbakedSwitch::cases)).apply((Applicative<SelectItemModel.UnbakedSwitch, ?>)i, SelectItemModel.UnbakedSwitch::new));
            return new Type<P, T>(switchCodec);
        }

        public static <T> MapCodec<List<SelectItemModel.SwitchCase<T>>> createCasesFieldCodec(Codec<T> valueCodec) {
            return SelectItemModel.SwitchCase.codec(valueCodec).listOf().validate(Type::validateCases).fieldOf("cases");
        }

        private static <T> DataResult<List<SelectItemModel.SwitchCase<T>>> validateCases(List<SelectItemModel.SwitchCase<T>> cases) {
            if (cases.isEmpty()) {
                return DataResult.error(() -> "Empty case list");
            }
            HashMultiset counts = HashMultiset.create();
            for (SelectItemModel.SwitchCase<T> c : cases) {
                counts.addAll(c.values());
            }
            if (counts.size() != counts.entrySet().size()) {
                return DataResult.error(() -> "Duplicate case conditions: " + counts.entrySet().stream().filter(e -> e.getCount() > 1).map(e -> e.getElement().toString()).collect(Collectors.joining(", ")));
            }
            return DataResult.success(cases);
        }
    }
}

