/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AdvancementTab.class})
public interface AdvancementTabAccessor {
    @Accessor(value="root")
    public AdvancementWidget fabric_getRoot();
}

