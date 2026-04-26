/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    public static final Codec<Map<EquipmentSlot, Float>> DROP_CHANCES_CODEC = Codec.either(Codec.FLOAT, Codec.unboundedMap(EquipmentSlot.CODEC, Codec.FLOAT)).xmap(either -> either.map(EquipmentTable::createForAllSlots, Function.identity()), provider -> {
        boolean dropChancesTheSame = provider.values().stream().distinct().count() == 1L;
        boolean allSlotsArePresent = provider.keySet().containsAll(EquipmentSlot.VALUES);
        if (dropChancesTheSame && allSlotsArePresent) {
            return Either.left(provider.values().stream().findFirst().orElse(Float.valueOf(0.0f)));
        }
        return Either.right(provider);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)LootTable.KEY_CODEC.fieldOf("loot_table")).forGetter(EquipmentTable::lootTable), DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply((Applicative<EquipmentTable, ?>)i, EquipmentTable::new));

    public EquipmentTable(ResourceKey<LootTable> lootTable, float dropChance) {
        this(lootTable, EquipmentTable.createForAllSlots(dropChance));
    }

    private static Map<EquipmentSlot, Float> createForAllSlots(float dropChance) {
        return EquipmentTable.createForAllSlots(List.of(EquipmentSlot.values()), dropChance);
    }

    private static Map<EquipmentSlot, Float> createForAllSlots(List<EquipmentSlot> slots, float dropChance) {
        HashMap<EquipmentSlot, Float> values = Maps.newHashMap();
        for (EquipmentSlot slot : slots) {
            values.put(slot, Float.valueOf(dropChance));
        }
        return values;
    }
}

