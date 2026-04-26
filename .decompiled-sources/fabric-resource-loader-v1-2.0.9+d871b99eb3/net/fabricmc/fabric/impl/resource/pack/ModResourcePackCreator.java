/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.pack;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesFactory;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

public class ModResourcePackCreator
implements RepositorySource {
    public static final String VANILLA = "vanilla";
    private static final String PROGRAMMER_ART = "programmer_art";
    private static final String HIGH_CONTRAST = "high_contrast";
    public static final Set<String> POST_CHANGE_HANDLE_REQUIRED = Set.of("vanilla", "programmer_art", "high_contrast");
    @VisibleForTesting
    public static final Predicate<Set<String>> BASE_PARENT = enabled -> enabled.contains(VANILLA);
    @VisibleForTesting
    public static final Predicate<Set<String>> PROGRAMMER_ART_PARENT = enabled -> enabled.contains(VANILLA) && enabled.contains(PROGRAMMER_ART);
    @VisibleForTesting
    public static final Predicate<Set<String>> HIGH_CONTRAST_PARENT = enabled -> enabled.contains(VANILLA) && enabled.contains(HIGH_CONTRAST);
    public static final PackSource RESOURCE_PACK_SOURCE = new PackSource(){

        @Override
        public Component decorate(Component packName) {
            return Component.translatable("pack.nameAndSource", packName, Component.translatable("pack.source.fabricmod"));
        }

        @Override
        public boolean shouldAddAutomatically() {
            return true;
        }
    };
    public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(PackType.CLIENT_RESOURCES);
    public static final int MAX_KNOWN_PACKS = Integer.getInteger("fabric-resource-loader-v1:maxKnownPacks", 1024);
    private final PackType type;
    private final PackSelectionConfig activationInfo;
    private final boolean forKnownPacksManager;

    public ModResourcePackCreator(PackType type) {
        this(type, false);
    }

    protected ModResourcePackCreator(PackType type, boolean forKnownPacksManager) {
        this.type = type;
        this.activationInfo = new PackSelectionConfig(!forKnownPacksManager, Pack.Position.TOP, false);
        this.forKnownPacksManager = forKnownPacksManager;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        this.registerModPack(consumer, null, BASE_PARENT);
        if (this.type == PackType.CLIENT_RESOURCES) {
            this.registerModPack(consumer, PROGRAMMER_ART, PROGRAMMER_ART_PARENT);
            this.registerModPack(consumer, HIGH_CONTRAST, HIGH_CONTRAST_PARENT);
        }
        ResourceLoaderImpl.registerBuiltinResourcePacks(this.type, consumer);
    }

    private void registerModPack(Consumer<Pack> consumer, @Nullable String subPath, Predicate<Set<String>> parents) {
        List<ModPackResources> packs = ModPackResourcesUtil.getModResourcePacks(FabricLoader.getInstance(), this.type, subPath);
        for (ModPackResources pack : packs) {
            Pack profile = Pack.readMetaAndCreate(pack.location(), new ModPackResourcesFactory(pack), this.type, this.activationInfo);
            if (profile == null) continue;
            if (!this.forKnownPacksManager) {
                ((FabricPack)((Object)profile)).fabric$setParentsPredicate(parents);
            }
            consumer.accept(profile);
        }
    }
}

