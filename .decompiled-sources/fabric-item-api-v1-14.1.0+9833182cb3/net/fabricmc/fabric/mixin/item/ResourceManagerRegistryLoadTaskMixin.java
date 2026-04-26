/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import net.fabricmc.fabric.impl.item.EnchantmentUtil;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ResourceManagerRegistryLoadTask.class})
public class ResourceManagerRegistryLoadTaskMixin {
    @WrapOperation(method={"lambda$load$2"}, at={@At(value="NEW", target="net/minecraft/resources/RegistryLoadTask$PendingRegistration")})
    private <T> RegistryLoadTask.PendingRegistration<?> modify(ResourceKey<T> key, Either<T, Exception> value, RegistrationInfo registrationInfo, Operation<RegistryLoadTask.PendingRegistration<T>> original, @Local(argsOnly=true) Resource resource) {
        Enchantment enchantment;
        Enchantment modified;
        T t;
        if (value.left().isPresent() && (t = value.left().get()) instanceof Enchantment && (modified = EnchantmentUtil.modify(key, enchantment = (Enchantment)t, EnchantmentUtil.determineSource(resource))) != null) {
            registrationInfo = new RegistrationInfo(Optional.empty(), registrationInfo.lifecycle());
            value = Either.left(modified);
        }
        return original.call(key, value, registrationInfo);
    }
}

