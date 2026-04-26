/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.menu;

import net.fabricmc.fabric.api.menu.v1.FabricMenuProvider;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={MenuProvider.class})
public interface MenuProviderMixin
extends FabricMenuProvider {
}

