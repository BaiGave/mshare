/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class ConventionalFluidTags {
    public static final TagKey<Fluid> LAVA = ConventionalFluidTags.register("lava");
    public static final TagKey<Fluid> WATER = ConventionalFluidTags.register("water");
    public static final TagKey<Fluid> MILK = ConventionalFluidTags.register("milk");
    public static final TagKey<Fluid> HONEY = ConventionalFluidTags.register("honey");
    public static final TagKey<Fluid> GASEOUS = ConventionalFluidTags.register("gaseous");
    public static final TagKey<Fluid> EXPERIENCE = ConventionalFluidTags.register("experience");
    public static final TagKey<Fluid> POTION = ConventionalFluidTags.register("potion");
    public static final TagKey<Fluid> SUSPICIOUS_STEW = ConventionalFluidTags.register("suspicious_stew");
    public static final TagKey<Fluid> MUSHROOM_STEW = ConventionalFluidTags.register("mushroom_stew");
    public static final TagKey<Fluid> RABBIT_STEW = ConventionalFluidTags.register("rabbit_stew");
    public static final TagKey<Fluid> BEETROOT_SOUP = ConventionalFluidTags.register("beetroot_soup");
    public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = ConventionalFluidTags.register("hidden_from_recipe_viewers");

    private ConventionalFluidTags() {
    }

    private static TagKey<Fluid> register(String tagId) {
        return TagRegistration.FLUID_TAG.registerC(tagId);
    }
}

