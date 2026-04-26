/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MappedRegistry.class})
public interface MappedRegistryAccessor<T> {
    @Accessor(value="registrationInfos")
    public Map<ResourceKey<T>, RegistrationInfo> fabric_getRegistrationInfos();
}

