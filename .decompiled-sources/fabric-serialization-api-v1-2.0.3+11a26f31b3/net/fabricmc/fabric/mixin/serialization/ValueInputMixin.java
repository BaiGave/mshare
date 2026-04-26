/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.serialization;

import net.fabricmc.fabric.api.serialization.v1.value.FabricValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={ValueInput.class})
public interface ValueInputMixin
extends FabricValueInput {
}

