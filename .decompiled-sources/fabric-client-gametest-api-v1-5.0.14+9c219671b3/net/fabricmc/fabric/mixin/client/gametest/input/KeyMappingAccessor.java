/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={KeyMapping.class})
public interface KeyMappingAccessor {
    @Accessor
    public InputConstants.Key getKey();
}

