/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.serialization;

import net.fabricmc.fabric.api.serialization.v1.value.FabricValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={ValueOutput.class})
public interface ValueOutputMixin
extends FabricValueOutput {
}

