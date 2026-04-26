/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SynchronizeRegistriesTask.class})
public abstract class SynchronizeRegistriesTaskMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("SynchronizeRegistriesTaskMixin");
    @Shadow
    @Final
    private List<KnownPack> requestedPacks;

    @Shadow
    protected abstract void sendRegistries(Consumer<Packet<?>> var1, Set<KnownPack> var2);

    @Inject(method={"handleResponse"}, at={@At(value="HEAD")}, cancellable=true)
    public void onSelectKnownPacks(List<KnownPack> clientKnownPacks, Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (new HashSet<KnownPack>(this.requestedPacks).containsAll(clientKnownPacks)) {
            this.sendRegistries(sender, Set.copyOf(clientKnownPacks));
            ci.cancel();
        }
    }

    @Inject(method={"sendRegistries"}, at={@At(value="HEAD")})
    public void syncRegistryAndTags(Consumer<Packet<?>> sender, Set<KnownPack> commonKnownPacks, CallbackInfo ci) {
        LOGGER.debug("Synchronizing registries with common known packs: {}", (Object)commonKnownPacks);
    }

    @Inject(method={"start"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendPacket(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (this.requestedPacks.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
            LOGGER.warn("Too many knownPacks: Found {}; max {}", (Object)this.requestedPacks.size(), (Object)ModResourcePackCreator.MAX_KNOWN_PACKS);
            sender.accept(new ClientboundSelectKnownPacks(this.requestedPacks.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS)));
            ci.cancel();
        }
    }
}

