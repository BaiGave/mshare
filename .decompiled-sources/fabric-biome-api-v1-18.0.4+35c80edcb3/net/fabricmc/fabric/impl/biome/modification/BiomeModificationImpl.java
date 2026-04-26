/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome.modification;

import com.google.common.base.Stopwatch;
import com.google.common.base.Suppliers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationContextImpl;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationMarker;
import net.fabricmc.fabric.impl.biome.modification.BiomeSelectionContextImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiomeModificationImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeModificationImpl.class);
    private static final Comparator<ModifierRecord> MODIFIER_ORDER_COMPARATOR = Comparator.comparingInt(r -> r.phase.ordinal()).thenComparingInt(r -> r.order).thenComparing(r -> r.id);
    public static final BiomeModificationImpl INSTANCE = new BiomeModificationImpl();
    private final List<ModifierRecord> modifiers = new ArrayList<ModifierRecord>();
    private boolean modifiersUnsorted = true;

    private BiomeModificationImpl() {
    }

    public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(modifier);
        this.modifiers.add(new ModifierRecord(phase, id, selector, modifier));
        this.modifiersUnsorted = true;
    }

    public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
        Objects.requireNonNull(selector);
        Objects.requireNonNull(modifier);
        this.modifiers.add(new ModifierRecord(phase, id, selector, modifier));
        this.modifiersUnsorted = true;
    }

    void changeOrder(Identifier id, int order) {
        this.modifiersUnsorted = true;
        for (ModifierRecord modifierRecord : this.modifiers) {
            if (!id.equals(modifierRecord.id)) continue;
            modifierRecord.setOrder(order);
        }
    }

    @TestOnly
    void clearModifiers() {
        this.modifiers.clear();
        this.modifiersUnsorted = true;
    }

    private List<ModifierRecord> getSortedModifiers() {
        if (this.modifiersUnsorted) {
            this.modifiers.sort(MODIFIER_ORDER_COMPARATOR);
            this.modifiersUnsorted = false;
        }
        return this.modifiers;
    }

    public void finalizeWorldGen(RegistryAccess impl) {
        Stopwatch sw = Stopwatch.createStarted();
        BiomeModificationMarker modificationTracker = (BiomeModificationMarker)((Object)impl);
        modificationTracker.fabric_markModified();
        HolderLookup.RegistryLookup biomes = impl.lookupOrThrow(Registries.BIOME);
        List<ResourceKey> keys = biomes.entrySet().stream().map(Map.Entry::getKey).sorted(Comparator.comparingInt(arg_0 -> BiomeModificationImpl.lambda$finalizeWorldGen$0((Registry)biomes, arg_0))).toList();
        List<ModifierRecord> sortedModifiers = this.getSortedModifiers();
        int biomesChanged = 0;
        int biomesProcessed = 0;
        int modifiersApplied = 0;
        for (ResourceKey key : keys) {
            Biome biome = (Biome)biomes.getValueOrThrow(key);
            ++biomesProcessed;
            BiomeSelectionContextImpl context = new BiomeSelectionContextImpl(impl, key, biome);
            BiomeModificationContextImpl modificationContext = null;
            for (ModifierRecord modifier : sortedModifiers) {
                if (!modifier.selector.test(context)) continue;
                LOGGER.trace("Applying modifier {} to {}", (Object)modifier, (Object)key.identifier());
                if (modificationContext == null) {
                    ++biomesChanged;
                    modificationContext = new BiomeModificationContextImpl(impl, biome);
                }
                modifier.apply(context, modificationContext);
                ++modifiersApplied;
            }
            if (modificationContext == null) continue;
            modificationContext.freeze();
            if (modificationContext.shouldRebuildFeatures()) {
                impl.lookupOrThrow(Registries.LEVEL_STEM).stream().forEach(levelStem -> {
                    levelStem.generator().featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(List.copyOf(levelStem.generator().getBiomeSource().possibleBiomes()), biomeHolder -> levelStem.generator().getBiomeGenerationSettings((Holder<Biome>)biomeHolder).features(), true));
                });
            }
            if (!(biomes instanceof MappedRegistry)) continue;
            MappedRegistry registry = (MappedRegistry)biomes;
            RegistrationInfo info = registry.registrationInfos.get(key);
            RegistrationInfo newInfo = new RegistrationInfo(Optional.empty(), info.lifecycle());
            registry.registrationInfos.put(key, newInfo);
        }
        if (biomesProcessed > 0) {
            LOGGER.info("Applied {} biome modifications to {} of {} new biomes in {}", modifiersApplied, biomesChanged, biomesProcessed, sw);
        }
    }

    private static /* synthetic */ int lambda$finalizeWorldGen$0(Registry biomes, ResourceKey key) {
        return biomes.getId((Biome)biomes.getValueOrThrow(key));
    }

    private static class ModifierRecord {
        private final ModificationPhase phase;
        private final Identifier id;
        private final Predicate<BiomeSelectionContext> selector;
        private final BiConsumer<BiomeSelectionContext, BiomeModificationContext> contextSensitiveModifier;
        private final Consumer<BiomeModificationContext> modifier;
        private int order;

        ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
            this.phase = phase;
            this.id = id;
            this.selector = selector;
            this.modifier = modifier;
            this.contextSensitiveModifier = null;
        }

        ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
            this.phase = phase;
            this.id = id;
            this.selector = selector;
            this.contextSensitiveModifier = modifier;
            this.modifier = null;
        }

        public String toString() {
            if (this.modifier != null) {
                return this.modifier.toString();
            }
            return this.contextSensitiveModifier.toString();
        }

        public void apply(BiomeSelectionContext context, BiomeModificationContextImpl modificationContext) {
            if (this.contextSensitiveModifier != null) {
                this.contextSensitiveModifier.accept(context, modificationContext);
            } else {
                this.modifier.accept(modificationContext);
            }
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }
}

