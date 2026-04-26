/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RegistryOps.class})
public interface RegistryOpsAccessor {
    @Accessor(value="lookupProvider")
    public RegistryOps.RegistryInfoLookup getRegistryInfoGetter();
}

