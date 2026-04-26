/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
    public static final float AXE_DISABLES_BLOCKING_FOR_SECONDS = 5.0f;
    public static final Codec<Weapon> CODEC = RecordCodecBuilder.create(i -> i.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("item_damage_per_attack", 1).forGetter(Weapon::itemDamagePerAttack), ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_blocking_for_seconds", Float.valueOf(0.0f)).forGetter(Weapon::disableBlockingForSeconds)).apply((Applicative<Weapon, ?>)i, Weapon::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Weapon> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Weapon::itemDamagePerAttack, ByteBufCodecs.FLOAT, Weapon::disableBlockingForSeconds, Weapon::new);

    public Weapon(int damagePerAttack) {
        this(damagePerAttack, 0.0f);
    }
}

