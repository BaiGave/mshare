/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import net.minecraft.resources.FileToIdConverter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={FileToIdConverter.class})
public interface FileToIdConverterAccessor {
    @Accessor(value="prefix")
    public String getPrefix();
}

