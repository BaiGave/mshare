/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen;

import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={TagBuilder.class})
public abstract class TagBuilderMixin
implements FabricTagBuilder {
    @Unique
    private boolean replace = false;

    @Shadow
    public abstract TagBuilder add(TagEntry var1);

    @Override
    public void fabric_setReplace(boolean replace) {
        this.replace = replace;
    }

    @Override
    public boolean fabric_isReplaced() {
        return this.replace;
    }

    @Override
    public void fabric_forceAddTag(Identifier tag) {
        this.add(new ForcedTagEntry(tag));
    }
}

