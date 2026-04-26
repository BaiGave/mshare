/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ConventionalEnchantmentTags {
    public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = ConventionalEnchantmentTags.register("increase_block_drops");
    public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = ConventionalEnchantmentTags.register("increase_entity_drops");
    public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = ConventionalEnchantmentTags.register("weapon_damage_enhancements");
    public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = ConventionalEnchantmentTags.register("entity_speed_enhancements");
    public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = ConventionalEnchantmentTags.register("entity_auxiliary_movement_enhancements");
    public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = ConventionalEnchantmentTags.register("entity_defense_enhancements");

    private ConventionalEnchantmentTags() {
    }

    private static TagKey<Enchantment> register(String tagId) {
        return TagRegistration.ENCHANTMENT_TAG.registerC(tagId);
    }
}

