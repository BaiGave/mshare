/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={DimensionType.class})
public interface DimensionTypeAccessor {
    @Accessor(value="attributes")
    @Mutable
    public void fabric_setAttributes(EnvironmentAttributeMap var1);
}

