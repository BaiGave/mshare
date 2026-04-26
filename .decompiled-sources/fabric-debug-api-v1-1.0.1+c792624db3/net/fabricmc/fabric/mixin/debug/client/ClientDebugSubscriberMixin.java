/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.debug.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Set;
import net.fabricmc.fabric.impl.debug.client.ClientDebugSubscriptionRegistryImpl;
import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientDebugSubscriber.class})
abstract class ClientDebugSubscriberMixin {
    ClientDebugSubscriberMixin() {
    }

    @Inject(method={"requestedSubscriptions"}, at={@At(value="RETURN")})
    private void addSubscribers(CallbackInfoReturnable<Set<DebugSubscription<?>>> cir, @Local(name={"subscriptions"}) Set<DebugSubscription<?>> subscriptions) {
        subscriptions.addAll(ClientDebugSubscriptionRegistryImpl.DEBUG_SUBSCRIPTIONS);
    }
}

