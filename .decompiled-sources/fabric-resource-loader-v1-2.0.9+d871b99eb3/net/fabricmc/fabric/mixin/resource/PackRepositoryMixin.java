/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PackRepository.class})
public abstract class PackRepositoryMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("PackRepositoryMixin");
    @Shadow
    @Final
    @Mutable
    public Set<RepositorySource> sources;
    @Shadow
    private Map<String, Pack> available;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void construct(RepositorySource[] resourcePackProviders, CallbackInfo info) {
        this.sources = new LinkedHashSet<RepositorySource>(this.sources);
        boolean shouldAddServerSource = false;
        for (RepositorySource source : this.sources) {
            if (!(source instanceof FolderRepositorySource) || ((FolderRepositorySource)source).packSource != PackSource.WORLD && ((FolderRepositorySource)source).packSource != PackSource.SERVER) continue;
            shouldAddServerSource = true;
            break;
        }
        if (shouldAddServerSource) {
            this.sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
        }
    }

    @Inject(method={"rebuildSelected"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;")})
    private void handleAutoEnableDisable(Collection<String> enabledNames, CallbackInfoReturnable<List<Pack>> cir, @Local(name={"selectedAndPresent"}) List<Pack> selectedAndPresent) {
        ModPackResourcesUtil.refreshAutoEnabledPacks(selectedAndPresent, this.available);
    }

    @Inject(method={"addPack"}, at={@At(value="INVOKE", target="Ljava/util/List;add(Ljava/lang/Object;)Z", shift=At.Shift.AFTER)})
    private void handleAutoEnable(String profile, CallbackInfoReturnable<Boolean> cir, @Local(name={"selectedCopy"}) List<Pack> selectedCopy) {
        if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
            ModPackResourcesUtil.refreshAutoEnabledPacks(selectedCopy, this.available);
        }
    }

    @Inject(method={"removePack"}, at={@At(value="INVOKE", target="Ljava/util/List;remove(Ljava/lang/Object;)Z")})
    private void handleAutoDisable(String profile, CallbackInfoReturnable<Boolean> cir, @Local(name={"selectedCopy"}) List<Pack> selectedCopy) {
        if (ModResourcePackCreator.POST_CHANGE_HANDLE_REQUIRED.contains(profile)) {
            Set currentlyEnabled = selectedCopy.stream().map(Pack::getId).collect(Collectors.toSet());
            selectedCopy.removeIf(p -> !((FabricPack)((Object)p)).fabric$parentsEnabled(currentlyEnabled));
            LOGGER.debug("[Fabric] Internal pack auto-removed upon disabling {}, result: {}", (Object)profile, (Object)selectedCopy.stream().map(Pack::getId).toList());
        }
    }
}

