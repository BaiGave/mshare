/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ConventionalEntityTypeTags {
    public static final TagKey<EntityType<?>> BOSSES = ConventionalEntityTypeTags.register("bosses");
    public static final TagKey<EntityType<?>> MINECARTS = ConventionalEntityTypeTags.register("minecarts");
    public static final TagKey<EntityType<?>> BOATS = ConventionalEntityTypeTags.register("boats");
    public static final TagKey<EntityType<?>> ITEM_FRAMES = ConventionalEntityTypeTags.register("item_frames");
    public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = ConventionalEntityTypeTags.register("capturing_not_supported");
    public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = ConventionalEntityTypeTags.register("teleporting_not_supported");

    private ConventionalEntityTypeTags() {
    }

    private static TagKey<EntityType<?>> register(String tagId) {
        return TagRegistration.ENTITY_TYPE_TAG.registerC(tagId);
    }
}

