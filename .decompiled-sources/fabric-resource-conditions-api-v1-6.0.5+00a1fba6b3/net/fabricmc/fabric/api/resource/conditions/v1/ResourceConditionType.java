/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.conditions.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.Optionull;
import net.minecraft.resources.Identifier;

public interface ResourceConditionType<T extends ResourceCondition> {
    public static final Codec<ResourceConditionType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id -> Optionull.mapOrElse(ResourceConditions.getConditionType(id), DataResult::success, () -> DataResult.error(() -> "Unknown resource condition key: " + String.valueOf(id))), ResourceConditionType::id);

    public Identifier id();

    public MapCodec<T> codec();

    public static <T extends ResourceCondition> ResourceConditionType<T> create(final Identifier id, final MapCodec<T> codec) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(codec, "codec cannot be null");
        return new ResourceConditionType<T>(){

            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public MapCodec<T> codec() {
                return codec;
            }
        };
    }
}

