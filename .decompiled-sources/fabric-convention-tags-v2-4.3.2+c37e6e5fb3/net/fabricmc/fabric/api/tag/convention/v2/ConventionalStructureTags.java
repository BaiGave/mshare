/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ConventionalStructureTags {
    public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = ConventionalStructureTags.register("hidden_from_displayers");
    public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = ConventionalStructureTags.register("hidden_from_locator_selection");

    private ConventionalStructureTags() {
    }

    private static TagKey<Structure> register(String tagId) {
        return TagRegistration.STRUCTURE_TAG.registerC(tagId);
    }
}

