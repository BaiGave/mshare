/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.fabric.impl.resource.DataResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;
import net.fabricmc.fabric.impl.resource.ResourceReloaderPhaseData;
import net.fabricmc.fabric.impl.resource.SetupMarkerResourceReloader;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public sealed class ResourceLoaderImpl
implements ResourceLoader
permits DataResourceLoaderImpl {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<PackType, ResourceLoaderImpl> IMPL_MAP = new EnumMap<PackType, ResourceLoaderImpl>(PackType.class);
    private static final Set<BuiltinPackResourcesEntry> BUILTIN_PACK_RESOURCES = new HashSet<BuiltinPackResourcesEntry>();
    private static final boolean DEBUG_RELOADERS_IDENTITY = TriState.fromSystemProperty("fabric.resource_loader.debug.reloaders_identity").orElse(FabricLoader.getInstance().isDevelopmentEnvironment());
    public static final boolean DEBUG_PROFILE_RESOURCE_RELOADERS = Boolean.getBoolean("fabric.resource_loader.debug.profile_resource_reloaders");
    private static final boolean DEBUG_RELOADERS_ORDER = Boolean.getBoolean("fabric.resource_loader.debug.reloaders_order");
    private final Map<Identifier, PreparableReloadListener> addedReloaders = new LinkedHashMap<Identifier, PreparableReloadListener>();
    private final Set<ReloaderOrder> reloadersOrdering = new LinkedHashSet<ReloaderOrder>();
    private final PackType type;

    public static ResourceLoaderImpl get(PackType type) {
        return IMPL_MAP.computeIfAbsent(type, target -> target == PackType.SERVER_DATA ? DataResourceLoaderImpl.INSTANCE : new ResourceLoaderImpl(type));
    }

    ResourceLoaderImpl(PackType type) {
        this.type = type;
    }

    protected boolean hasResourceReloader(Identifier id) {
        return this.addedReloaders.containsKey(id);
    }

    protected final void checkUniqueResourceReloader(Identifier id) {
        if (this.hasResourceReloader(id)) {
            throw new IllegalStateException("Tried to register resource listener %s twice!".formatted(id));
        }
    }

    @Override
    public void registerReloadListener(Identifier id, PreparableReloadListener listener) {
        Objects.requireNonNull(id, "The listener identifier should not be null.");
        Objects.requireNonNull(listener, "The listener should not be null.");
        this.checkUniqueResourceReloader(id);
        for (Map.Entry<Identifier, PreparableReloadListener> entry : this.addedReloaders.entrySet()) {
            if (entry.getValue() != listener) continue;
            throw new IllegalStateException("Resource listener with ID %s already in resource listener set with ID %s!".formatted(id, entry.getKey()));
        }
        this.addedReloaders.put(id, listener);
    }

    @Override
    public void addListenerOrdering(Identifier firstListener, Identifier secondListener) {
        Objects.requireNonNull(firstListener, "The first listener identifier should not be null.");
        Objects.requireNonNull(secondListener, "The second listener identifier should not be null.");
        if (firstListener.equals(secondListener)) {
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");
        }
        this.reloadersOrdering.add(new ReloaderOrder(firstListener, secondListener));
    }

    private Identifier getResourceReloaderIdForSorting(PreparableReloadListener reloader) {
        if (reloader instanceof FabricResourceReloader) {
            FabricResourceReloader identifiable = (FabricResourceReloader)reloader;
            return identifiable.fabric$getId();
        }
        if (DEBUG_RELOADERS_IDENTITY) {
            LOGGER.warn("The resource listener at {} does not use identifiable registration making ordering support more difficult for other modders.", (Object)reloader.getClass().getName());
        }
        return Identifier.fromNamespaceAndPath("unknown", "private/" + reloader.getClass().getName().replace(".", "/").replace("$", "_").toLowerCase(Locale.ROOT));
    }

    public static List<PreparableReloadListener> sort(PackType type, List<PreparableReloadListener> listeners) {
        if (type == null) {
            return listeners;
        }
        ResourceLoaderImpl instance = ResourceLoaderImpl.get(type);
        ArrayList<PreparableReloadListener> mutable = new ArrayList<PreparableReloadListener>(listeners);
        instance.sort(mutable);
        return Collections.unmodifiableList(mutable);
    }

    protected Set<Map.Entry<Identifier, PreparableReloadListener>> collectReloadersToAdd(@Nullable SetupMarkerResourceReloader setupMarker) {
        return new LinkedHashSet<Map.Entry<Identifier, PreparableReloadListener>>(this.addedReloaders.entrySet());
    }

    private void sort(List<PreparableReloadListener> reloaders) {
        SetupMarkerResourceReloader setupReloader = this.extractSetupMarker(reloaders);
        Set<Map.Entry<Identifier, PreparableReloadListener>> reloadersToAdd = this.collectReloadersToAdd(setupReloader);
        reloadersToAdd.stream().map(Map.Entry::getValue).forEach(reloaders::remove);
        Object2ObjectOpenHashMap<Object, ResourceReloaderPhaseData> runtimePhases = new Object2ObjectOpenHashMap<Object, ResourceReloaderPhaseData>();
        Iterator<PreparableReloadListener> itPhases = reloaders.iterator();
        ResourceReloaderPhaseData last = new ResourceReloaderPhaseData(ResourceReloaderKeys.BEFORE_VANILLA, null);
        last.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
        runtimePhases.put(last.id, last);
        while (itPhases.hasNext()) {
            PreparableReloadListener currentReloader = itPhases.next();
            Iterator id = this.getResourceReloaderIdForSorting(currentReloader);
            ResourceReloaderPhaseData resourceReloaderPhaseData = new ResourceReloaderPhaseData((Identifier)((Object)id), currentReloader);
            resourceReloaderPhaseData.setVanillaStatus(ResourceReloaderPhaseData.VanillaStatus.VANILLA);
            runtimePhases.put(id, resourceReloaderPhaseData);
            SortableNode.link(last, resourceReloaderPhaseData);
            last = resourceReloaderPhaseData;
        }
        ResourceReloaderPhaseData.AfterVanilla afterVanilla = new ResourceReloaderPhaseData.AfterVanilla(ResourceReloaderKeys.AFTER_VANILLA);
        runtimePhases.put(afterVanilla.id, afterVanilla);
        SortableNode.link(last, afterVanilla);
        for (Map.Entry entry : reloadersToAdd) {
            ResourceReloaderPhaseData phase = new ResourceReloaderPhaseData((Identifier)entry.getKey(), (PreparableReloadListener)entry.getValue());
            runtimePhases.put(phase.id, phase);
        }
        for (ReloaderOrder reloaderOrder : this.reloadersOrdering) {
            ResourceReloaderPhaseData second;
            ResourceReloaderPhaseData first = (ResourceReloaderPhaseData)runtimePhases.get(reloaderOrder.first);
            if (first == null || (second = (ResourceReloaderPhaseData)runtimePhases.get(reloaderOrder.second)) == null) continue;
            SortableNode.link(first, second);
        }
        for (ResourceReloaderPhaseData resourceReloaderPhaseData : runtimePhases.values()) {
            if (resourceReloaderPhaseData == afterVanilla || resourceReloaderPhaseData.vanillaStatus != ResourceReloaderPhaseData.VanillaStatus.NONE && resourceReloaderPhaseData.vanillaStatus != ResourceReloaderPhaseData.VanillaStatus.AFTER) continue;
            SortableNode.link(afterVanilla, resourceReloaderPhaseData);
        }
        ArrayList phases = new ArrayList(runtimePhases.values());
        NodeSorting.sort(phases, "resource reloaders", Comparator.comparing(data -> data.id));
        reloaders.clear();
        if (setupReloader != null) {
            reloaders.add(setupReloader);
        }
        for (ResourceReloaderPhaseData phase : phases) {
            if (phase.resourceReloader == null) continue;
            reloaders.add(phase.resourceReloader);
        }
        if (DEBUG_RELOADERS_ORDER) {
            LOGGER.info("Sorted reloaders: {}", (Object)phases.stream().map(data -> {
                Object str = data.id.toString();
                if (data.resourceReloader == null) {
                    str = (String)str + " (virtual)";
                }
                return str;
            }).collect(Collectors.joining(", ")));
        }
    }

    private @Nullable SetupMarkerResourceReloader extractSetupMarker(List<PreparableReloadListener> reloaders) {
        if (this.type == PackType.CLIENT_RESOURCES) {
            return null;
        }
        Iterator<PreparableReloadListener> it = reloaders.iterator();
        while (it.hasNext()) {
            PreparableReloadListener preparableReloadListener = it.next();
            if (!(preparableReloadListener instanceof SetupMarkerResourceReloader)) continue;
            SetupMarkerResourceReloader marker = (SetupMarkerResourceReloader)preparableReloadListener;
            it.remove();
            return marker;
        }
        throw new IllegalStateException("No SetupMarkerResourceReloader found in reloaders!");
    }

    public static boolean registerBuiltinPack(Identifier id, String subPath, ModContainer container, Component displayName, PackActivationType activationType) {
        List<Path> paths = container.getRootPaths();
        String separator = paths.getFirst().getFileSystem().getSeparator();
        subPath = subPath.replace("/", separator);
        ModNioPackResources resourcePack = ModNioPackResources.create(id.toString(), container, subPath, PackType.CLIENT_RESOURCES, activationType, false);
        ModNioPackResources dataPack = ModNioPackResources.create(id.toString(), container, subPath, PackType.SERVER_DATA, activationType, false);
        if (resourcePack == null && dataPack == null) {
            return false;
        }
        if (resourcePack != null) {
            BUILTIN_PACK_RESOURCES.add(new BuiltinPackResourcesEntry(displayName, resourcePack));
        }
        if (dataPack != null) {
            BUILTIN_PACK_RESOURCES.add(new BuiltinPackResourcesEntry(displayName, dataPack));
        }
        return true;
    }

    public static boolean registerBuiltinPack(Identifier id, String subPath, ModContainer container, PackActivationType activationType) {
        return ResourceLoaderImpl.registerBuiltinPack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()), activationType);
    }

    public static void registerBuiltinResourcePacks(PackType type, Consumer<Pack> consumer) {
        for (BuiltinPackResourcesEntry entry : BUILTIN_PACK_RESOURCES) {
            final ModNioPackResources pack = entry.packResources();
            if (pack.getNamespaces(type).isEmpty()) continue;
            PackLocationInfo info = new PackLocationInfo(pack.packId(), entry.displayName(), new BuiltinModPackSource(pack.getFabricModMetadata().getName()), pack.knownPackInfo());
            PackSelectionConfig selectionInfo = new PackSelectionConfig(pack.getActivationType() == PackActivationType.ALWAYS_ENABLED, Pack.Position.TOP, false);
            Pack profile = Pack.readMetaAndCreate(info, new Pack.ResourcesSupplier(){

                @Override
                public PackResources openPrimary(PackLocationInfo location) {
                    return pack;
                }

                @Override
                public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                    if (metadata.overlays().isEmpty()) {
                        return pack;
                    }
                    ArrayList<PackResources> overlays = new ArrayList<PackResources>(metadata.overlays().size());
                    for (String overlay : metadata.overlays()) {
                        overlays.add(pack.createOverlay(overlay));
                    }
                    return new CompositePackResources(pack, overlays);
                }
            }, type, selectionInfo);
            consumer.accept(profile);
        }
    }

    private record ReloaderOrder(Identifier first, Identifier second) {
    }

    private record BuiltinPackResourcesEntry(Component displayName, ModNioPackResources packResources) {
    }
}

