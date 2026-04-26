/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record BlocksAttacks(float blockDelaySeconds, float disableCooldownScale, List<DamageReduction> damageReductions, ItemDamageFunction itemDamage, Optional<HolderSet<DamageType>> bypassedBy, Optional<Holder<SoundEvent>> blockSound, Optional<Holder<SoundEvent>> disableSound) {
    public static final Codec<BlocksAttacks> CODEC = RecordCodecBuilder.create(i -> i.group(ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds", Float.valueOf(0.0f)).forGetter(BlocksAttacks::blockDelaySeconds), ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale", Float.valueOf(1.0f)).forGetter(BlocksAttacks::disableCooldownScale), DamageReduction.CODEC.listOf().optionalFieldOf("damage_reductions", List.of(new DamageReduction(90.0f, Optional.empty(), 0.0f, 1.0f))).forGetter(BlocksAttacks::damageReductions), ItemDamageFunction.CODEC.optionalFieldOf("item_damage", ItemDamageFunction.DEFAULT).forGetter(BlocksAttacks::itemDamage), RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacks::bypassedBy), SoundEvent.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacks::blockSound), SoundEvent.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacks::disableSound)).apply((Applicative<BlocksAttacks, ?>)i, BlocksAttacks::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlocksAttacks::blockDelaySeconds, ByteBufCodecs.FLOAT, BlocksAttacks::disableCooldownScale, DamageReduction.STREAM_CODEC.apply(ByteBufCodecs.list()), BlocksAttacks::damageReductions, ItemDamageFunction.STREAM_CODEC, BlocksAttacks::itemDamage, ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), BlocksAttacks::bypassedBy, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::blockSound, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::disableSound, BlocksAttacks::new);

    public void onBlocked(ServerLevel level, LivingEntity user) {
        this.blockSound.ifPresent(sound -> level.playSound(null, user.getX(), user.getY(), user.getZ(), (Holder<SoundEvent>)sound, user.getSoundSource(), 1.0f, 0.8f + level.getRandom().nextFloat() * 0.4f));
    }

    public void disable(ServerLevel level, LivingEntity user, float baseSeconds, ItemStack blockingWith) {
        int cooldownTicks = this.disableBlockingForTicks(baseSeconds);
        if (cooldownTicks > 0) {
            if (user instanceof Player) {
                Player player = (Player)user;
                player.getCooldowns().addCooldown(blockingWith, cooldownTicks);
            }
            user.stopUsingItem();
            this.disableSound.ifPresent(sound -> level.playSound(null, user.getX(), user.getY(), user.getZ(), (Holder<SoundEvent>)sound, user.getSoundSource(), 0.8f, 0.8f + level.getRandom().nextFloat() * 0.4f));
        }
    }

    public void hurtBlockingItem(Level level, ItemStack item, LivingEntity user, InteractionHand hand, float damage) {
        int itemDamage;
        if (!(user instanceof Player)) {
            return;
        }
        Player player = (Player)user;
        if (!level.isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(item.getItem()));
        }
        if ((itemDamage = this.itemDamage.apply(damage)) > 0) {
            item.hurtAndBreak(itemDamage, user, hand.asEquipmentSlot());
        }
    }

    private int disableBlockingForTicks(float baseSeconds) {
        float seconds = baseSeconds * this.disableCooldownScale;
        if (seconds > 0.0f) {
            return Math.round(seconds * 20.0f);
        }
        return 0;
    }

    public int blockDelayTicks() {
        return Math.round(this.blockDelaySeconds * 20.0f);
    }

    public float resolveBlockedDamage(DamageSource source, float dealtDamage, double angle) {
        float blockedDamage = 0.0f;
        for (DamageReduction reduction : this.damageReductions) {
            blockedDamage += reduction.resolve(source, dealtDamage, angle);
        }
        return Mth.clamp(blockedDamage, 0.0f, dealtDamage);
    }

    public record ItemDamageFunction(float threshold, float base, float factor) {
        public static final Codec<ItemDamageFunction> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("threshold")).forGetter(ItemDamageFunction::threshold), ((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(ItemDamageFunction::base), ((MapCodec)Codec.FLOAT.fieldOf("factor")).forGetter(ItemDamageFunction::factor)).apply((Applicative<ItemDamageFunction, ?>)i, ItemDamageFunction::new));
        public static final StreamCodec<ByteBuf, ItemDamageFunction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ItemDamageFunction::threshold, ByteBufCodecs.FLOAT, ItemDamageFunction::base, ByteBufCodecs.FLOAT, ItemDamageFunction::factor, ItemDamageFunction::new);
        public static final ItemDamageFunction DEFAULT = new ItemDamageFunction(1.0f, 0.0f, 1.0f);

        public int apply(float dealtDamage) {
            if (dealtDamage < this.threshold) {
                return 0;
            }
            return Mth.floor(this.base + this.factor * dealtDamage);
        }
    }

    public record DamageReduction(float horizontalBlockingAngle, Optional<HolderSet<DamageType>> type, float base, float factor) {
        public static final Codec<DamageReduction> CODEC = RecordCodecBuilder.create(i -> i.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", Float.valueOf(90.0f)).forGetter(DamageReduction::horizontalBlockingAngle), RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageReduction::type), ((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(DamageReduction::base), ((MapCodec)Codec.FLOAT.fieldOf("factor")).forGetter(DamageReduction::factor)).apply((Applicative<DamageReduction, ?>)i, DamageReduction::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, DamageReduction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, DamageReduction::horizontalBlockingAngle, ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), DamageReduction::type, ByteBufCodecs.FLOAT, DamageReduction::base, ByteBufCodecs.FLOAT, DamageReduction::factor, DamageReduction::new);

        public float resolve(DamageSource source, float dealtDamage, double angle) {
            if (angle > (double)((float)Math.PI / 180 * this.horizontalBlockingAngle)) {
                return 0.0f;
            }
            if (this.type.isPresent() && !this.type.get().contains(source.typeHolder())) {
                return 0.0f;
            }
            return Mth.clamp(this.base + this.factor * dealtDamage, 0.0f, dealtDamage);
        }
    }
}

