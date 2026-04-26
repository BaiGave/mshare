/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.creativetab;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CreativeModeTab.class})
public interface CreativeModeTabAccessor {
    @Accessor
    @Mutable
    @Final
    public void setRow(CreativeModeTab.Row var1);

    @Accessor
    @Mutable
    @Final
    public void setColumn(int var1);
}

