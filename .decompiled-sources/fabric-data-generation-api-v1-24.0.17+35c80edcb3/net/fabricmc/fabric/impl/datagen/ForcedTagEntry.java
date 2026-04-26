/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen;

import java.util.function.Predicate;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

public class ForcedTagEntry
extends TagEntry {
    public ForcedTagEntry(Identifier id) {
        super(id, true, true);
    }

    @Override
    public boolean verifyIfPresent(Predicate<Identifier> objectExistsTest, Predicate<Identifier> tagExistsTest) {
        return true;
    }
}

