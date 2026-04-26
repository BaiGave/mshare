/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockGetter.class})
public interface BlockGetterMixin
extends FabricBlockGetter {
}

