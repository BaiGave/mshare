/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AdvancementWidget.class})
public interface AdvancementWidgetAccessor {
    @Accessor(value="progress")
    public @Nullable AdvancementProgress fabric_getProgress();
}

