/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ResourceManagerRegistryLoadTask.class})
public class ResourceManagerRegistryLoadTaskMixin {
    @ModifyExpressionValue(method={"lambda$load$2"}, at={@At(value="NEW", target="net/minecraft/resources/RegistryLoadTask$PendingRegistration")})
    private RegistryLoadTask.PendingRegistration<?> load(RegistryLoadTask.PendingRegistration<?> original) {
        if (original.value().right().isPresent() && original.value().right().get() == ResourceConditionsImpl.DISABLED_RESOURCE_EXCEPTION) {
            return null;
        }
        return original;
    }
}

