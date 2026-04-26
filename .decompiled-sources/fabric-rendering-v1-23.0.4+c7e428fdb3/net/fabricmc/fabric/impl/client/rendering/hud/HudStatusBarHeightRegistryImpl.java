/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.hud;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.StatusBarHeightProvider;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.hud.HudLayer;
import net.fabricmc.fabric.mixin.client.rendering.GuiAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HudStatusBarHeightRegistryImpl
implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabric-rendering-v1");
    static final int DEFAULT_HEIGHT = 39;
    static final int HELD_ITEM_TOOLTIP_HEIGHT = 20;
    static final int OVERLAY_MESSAGE_HEIGHT = 29;
    static final int TEXT_HEIGHT_DELTA = 9;
    static final StatusBarHeightProvider HEALTH_BAR = player -> {
        Gui hud = Minecraft.getInstance().gui;
        int playerHealth = Mth.ceil(player.getHealth());
        int displayHealth = ((GuiAccessor)((Object)hud)).fabric$getRenderHealthValue();
        float maxHealth = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(displayHealth, playerHealth));
        int absorptionAmount = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealth + (float)absorptionAmount) / 2.0f / 10.0f);
        int rowShift = Math.max(10 - (healthRows - 2), 3);
        return 10 + (healthRows - 1) * rowShift;
    };
    static final StatusBarHeightProvider ARMOR_BAR = player -> player.getArmorValue() > 0 ? 10 : 0;
    static final StatusBarHeightProvider MOUNT_HEALTH = player -> {
        Gui hud = Minecraft.getInstance().gui;
        LivingEntity livingEntity = ((GuiAccessor)((Object)hud)).fabric$callGetRiddenEntity();
        int vehicleMaxHearts = ((GuiAccessor)((Object)hud)).fabric$callGetHeartCount(livingEntity);
        return ((GuiAccessor)((Object)hud)).fabric$callGetHeartRows(vehicleMaxHearts) * 10;
    };
    static final StatusBarHeightProvider FOOD_BAR = player -> {
        Gui hud = Minecraft.getInstance().gui;
        LivingEntity livingEntity = ((GuiAccessor)((Object)hud)).fabric$callGetRiddenEntity();
        return ((GuiAccessor)((Object)hud)).fabric$callGetHeartCount(livingEntity) == 0 ? 10 : 0;
    };
    static final StatusBarHeightProvider AIR_BAR = player -> {
        int maxAirSupply = player.getMaxAirSupply();
        int airSupply = Math.clamp((long)player.getAirSupply(), 0, maxAirSupply);
        boolean isInWater = player.isEyeInFluid(FluidTags.WATER);
        return isInWater || airSupply < maxAirSupply ? 10 : 0;
    };
    static final Map<Identifier, ResolvedHeightProvider> RESOLVED_VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(VanillaHudElements.HEALTH_BAR, ResolvedHeightProvider.ZERO, VanillaHudElements.ARMOR_BAR, HEALTH_BAR::getStatusBarHeight, VanillaHudElements.MOUNT_HEALTH, ResolvedHeightProvider.ZERO, VanillaHudElements.FOOD_BAR, ResolvedHeightProvider.ZERO, VanillaHudElements.AIR_BAR, HudStatusBarHeightRegistryImpl.reduceToIntFunctions(MOUNT_HEALTH, FOOD_BAR, Integer::sum));
    static final Map<Identifier, StatusBarHeightProvider> LEFT_VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(VanillaHudElements.HEALTH_BAR, HEALTH_BAR, VanillaHudElements.ARMOR_BAR, ARMOR_BAR);
    static final Map<Identifier, StatusBarHeightProvider> RIGHT_VANILLA_HEIGHT_PROVIDERS = ImmutableMap.of(VanillaHudElements.MOUNT_HEALTH, MOUNT_HEALTH, VanillaHudElements.FOOD_BAR, FOOD_BAR, VanillaHudElements.AIR_BAR, AIR_BAR);
    static final Map<Identifier, StatusBarHeightProvider> LEFT_HEIGHT_PROVIDERS = new HashMap<Identifier, StatusBarHeightProvider>(LEFT_VANILLA_HEIGHT_PROVIDERS);
    static final Map<Identifier, StatusBarHeightProvider> RIGHT_HEIGHT_PROVIDERS = new HashMap<Identifier, StatusBarHeightProvider>(RIGHT_VANILLA_HEIGHT_PROVIDERS);
    static @Nullable Map<Identifier, ResolvedHeightProvider> resolvedHeightProviders;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> HudStatusBarHeightRegistryImpl.init());
    }

    public static void addLeft(Identifier id, StatusBarHeightProvider heightProvider) {
        if (resolvedHeightProviders != null) {
            throw new IllegalStateException("Height provider registry already frozen!");
        }
        LEFT_HEIGHT_PROVIDERS.put(id, heightProvider);
    }

    public static void addRight(Identifier id, StatusBarHeightProvider heightProvider) {
        if (resolvedHeightProviders != null) {
            throw new IllegalStateException("Height provider registry already frozen!");
        }
        RIGHT_HEIGHT_PROVIDERS.put(id, heightProvider);
    }

    public static int getHeight(Identifier id) {
        if (resolvedHeightProviders == null) {
            throw new IllegalStateException("Trying to get status bar height for " + String.valueOf(id) + " too early");
        }
        if (!resolvedHeightProviders.containsKey(id)) {
            throw new IllegalArgumentException("Unknown status bar: " + String.valueOf(id));
        }
        Player player = ((GuiAccessor)((Object)Minecraft.getInstance().gui)).fabric$callGetCameraPlayer();
        if (player == null) {
            throw new IllegalStateException("Trying to get status bar height for " + String.valueOf(id) + " without a camera player");
        }
        return 39 + resolvedHeightProviders.get(id).getResolvedHeight(player);
    }

    static void init() {
        if (LEFT_VANILLA_HEIGHT_PROVIDERS.equals(LEFT_HEIGHT_PROVIDERS) && RIGHT_VANILLA_HEIGHT_PROVIDERS.equals(RIGHT_HEIGHT_PROVIDERS)) {
            resolvedHeightProviders = RESOLVED_VANILLA_HEIGHT_PROVIDERS;
        } else {
            LinkedHashMap<Identifier, ResolvedHeightProvider> resolvedHeightProviders = new LinkedHashMap<Identifier, ResolvedHeightProvider>();
            ResolvedHeightProvider maxLeftHeightProvider = HudStatusBarHeightRegistryImpl.resolveHeightProviders(LEFT_HEIGHT_PROVIDERS, resolvedHeightProviders::put);
            ResolvedHeightProvider maxRightHeightProvider = HudStatusBarHeightRegistryImpl.resolveHeightProviders(RIGHT_HEIGHT_PROVIDERS, resolvedHeightProviders::put);
            HudStatusBarHeightRegistryImpl.applyVanillaHeightProviders(resolvedHeightProviders, HudStatusBarHeightRegistryImpl.reduceToIntFunctions(maxLeftHeightProvider, maxRightHeightProvider, Math::max));
            HudStatusBarHeightRegistryImpl.resolvedHeightProviders = ImmutableMap.copyOf(resolvedHeightProviders);
        }
    }

    private static ResolvedHeightProvider resolveHeightProviders(Map<Identifier, StatusBarHeightProvider> heightProviderLookup, BiConsumer<Identifier, ResolvedHeightProvider> heightProviderConsumer) {
        SequencedSet<Identifier> orderedHeightProviders = HudStatusBarHeightRegistryImpl.getOrderedHeightProviders(heightProviderLookup);
        Sets.SetView<Identifier> unregisteredHudElements = Sets.difference(heightProviderLookup.keySet(), orderedHeightProviders);
        if (!unregisteredHudElements.isEmpty()) {
            throw new IllegalStateException("Unregistered hud elements: " + String.valueOf(unregisteredHudElements));
        }
        for (Identifier id : heightProviderLookup.keySet()) {
            ResolvedHeightProvider heightProvider = HudStatusBarHeightRegistryImpl.resolveHeightProvider(id, heightProviderLookup, orderedHeightProviders);
            heightProviderConsumer.accept(id, heightProvider);
        }
        return HudStatusBarHeightRegistryImpl.resolveMaximumHeightProvider((Identifier)orderedHeightProviders.getLast(), heightProviderLookup, orderedHeightProviders);
    }

    private static SequencedSet<Identifier> getOrderedHeightProviders(Map<Identifier, StatusBarHeightProvider> heightProviderLookup) {
        LinkedHashSet<Identifier> orderedHeightProviders = new LinkedHashSet<Identifier>();
        for (Identifier identifier : RESOLVED_VANILLA_HEIGHT_PROVIDERS.keySet()) {
            for (HudLayer hudLayer : HudElementRegistryImpl.ROOT_ELEMENTS.get(identifier).layers()) {
                HudStatusBarHeightRegistryImpl.addOrderedHeightProvider(hudLayer, heightProviderLookup, orderedHeightProviders::add);
            }
        }
        for (Map.Entry entry : HudElementRegistryImpl.ROOT_ELEMENTS.entrySet()) {
            if (RESOLVED_VANILLA_HEIGHT_PROVIDERS.containsKey(entry.getKey())) continue;
            for (HudLayer hudLayer : ((HudElementRegistryImpl.RootLayer)entry.getValue()).layers()) {
                HudStatusBarHeightRegistryImpl.addOrderedHeightProvider(hudLayer, heightProviderLookup, orderedHeightProviders::add);
            }
        }
        return orderedHeightProviders;
    }

    private static void addOrderedHeightProvider(HudLayer hudLayer, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, Consumer<Identifier> heightProviderConsumer) {
        if (!hudLayer.isRemoved() && heightProviderLookup.containsKey(hudLayer.id())) {
            heightProviderConsumer.accept(hudLayer.id());
        }
    }

    private static ResolvedHeightProvider resolveHeightProvider(Identifier id, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
        ResolvedHeightProvider heightProvider = ResolvedHeightProvider.ZERO;
        for (Identifier heightProviderLocation : orderedHeightProviders) {
            if (heightProviderLocation.equals(id)) {
                return heightProvider;
            }
            if (!heightProviderLookup.containsKey(heightProviderLocation)) continue;
            heightProvider = HudStatusBarHeightRegistryImpl.reduceToIntFunctions(heightProvider, heightProviderLookup.get(heightProviderLocation), Integer::sum);
        }
        throw new IllegalStateException("Unknown height provider: " + String.valueOf(id));
    }

    private static ResolvedHeightProvider resolveMaximumHeightProvider(Identifier id, Map<Identifier, StatusBarHeightProvider> heightProviderLookup, SequencedCollection<Identifier> orderedHeightProviders) {
        ResolvedHeightProvider heightProvider = HudStatusBarHeightRegistryImpl.resolveHeightProvider(id, heightProviderLookup, orderedHeightProviders);
        return HudStatusBarHeightRegistryImpl.reduceToIntFunctions(heightProviderLookup.get(id), heightProvider, Integer::sum);
    }

    private static ResolvedHeightProvider reduceToIntFunctions(ToIntFunction<Player> first, ToIntFunction<Player> second, IntBinaryOperator operator) {
        return player -> operator.applyAsInt(first.applyAsInt(player), second.applyAsInt(player));
    }

    private static void applyVanillaHeightProviders(Map<Identifier, ResolvedHeightProvider> resolvedHeightProviders, ResolvedHeightProvider maxHeightProvider) {
        for (Map.Entry<Identifier, ResolvedHeightProvider> entry : RESOLVED_VANILLA_HEIGHT_PROVIDERS.entrySet()) {
            if (HudStatusBarHeightRegistryImpl.isVanillaHeightProvider(entry.getKey())) {
                ResolvedHeightProvider expectedHeightProvider = entry.getValue();
                ResolvedHeightProvider actualHeightProvider = resolvedHeightProviders.put(entry.getKey(), expectedHeightProvider);
                Objects.requireNonNull(actualHeightProvider, () -> "resolved height provider " + String.valueOf(entry.getKey()) + " is null");
                HudStatusBarHeightRegistryImpl.replaceVanillaElement(entry.getKey(), HudStatusBarHeightRegistryImpl.reduceToIntFunctions(expectedHeightProvider, actualHeightProvider, (i1, i2) -> i1 - i2));
                continue;
            }
            LOGGER.debug("Skipped wrapping hud element {} for applying height provider offsets", (Object)entry.getKey());
        }
        HudStatusBarHeightRegistryImpl.replaceVanillaElement(VanillaHudElements.HELD_ITEM_TOOLTIP, player -> 20 - Math.max(20, maxHeightProvider.getResolvedHeight(player)));
        HudStatusBarHeightRegistryImpl.replaceVanillaElement(VanillaHudElements.OVERLAY_MESSAGE, player -> 29 - Math.max(29, maxHeightProvider.getResolvedHeight(player) + 9));
    }

    private static boolean isVanillaHeightProvider(Identifier id) {
        if (LEFT_HEIGHT_PROVIDERS.containsKey(id) && LEFT_HEIGHT_PROVIDERS.get(id) == LEFT_VANILLA_HEIGHT_PROVIDERS.get(id)) {
            return true;
        }
        return RIGHT_HEIGHT_PROVIDERS.containsKey(id) && RIGHT_HEIGHT_PROVIDERS.get(id) == RIGHT_VANILLA_HEIGHT_PROVIDERS.get(id);
    }

    private static void replaceVanillaElement(Identifier id, ResolvedHeightProvider heightProvider) {
        HudElementRegistry.replaceElement(id, layer -> (graphics, deltaTracker) -> {
            int height;
            Player player = ((GuiAccessor)((Object)Minecraft.getInstance().gui)).fabric$callGetCameraPlayer();
            int n = height = player != null ? heightProvider.getResolvedHeight(player) : 0;
            if (height != 0) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(0.0f, height);
            }
            layer.extractRenderState(graphics, deltaTracker);
            if (height != 0) {
                graphics.pose().popMatrix();
            }
        });
    }

    @FunctionalInterface
    public static interface ResolvedHeightProvider
    extends ToIntFunction<Player> {
        public static final ResolvedHeightProvider ZERO = player -> 0;

        public int getResolvedHeight(Player var1);

        @Override
        @ApiStatus.NonExtendable
        default public int applyAsInt(Player player) {
            return this.getResolvedHeight(player);
        }
    }
}

