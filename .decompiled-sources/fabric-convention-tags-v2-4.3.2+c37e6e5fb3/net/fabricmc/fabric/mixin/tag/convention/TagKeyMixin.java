/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag.convention;

import net.fabricmc.fabric.api.tag.FabricTagKey;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={TagKey.class})
public interface TagKeyMixin
extends FabricTagKey {
}

