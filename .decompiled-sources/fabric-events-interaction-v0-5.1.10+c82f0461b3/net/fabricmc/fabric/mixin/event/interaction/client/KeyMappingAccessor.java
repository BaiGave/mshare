/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={KeyMapping.class})
public interface KeyMappingAccessor {
    @Accessor(value="clickCount")
    public int fabric_getTimesPressed();
}

