/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.debug;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.debug.ServerDebugSubscribers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ServerDebugSubscribers.class})
abstract class ServerDebugSubscribersMixin {
    ServerDebugSubscribersMixin() {
    }

    @WrapOperation(method={"hasRequiredPermissions"}, at={@At(value="MIXINEXTRAS:EXPRESSION")})
    @Definition(id="IS_RUNNING_IN_IDE", field={"Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"})
    @Expression(value={"IS_RUNNING_IN_IDE"})
    private boolean requireInIde(Operation<Boolean> original) {
        return original.call(new Object[0]) != false || FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}

