/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.conditions;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceConditionsImpl
implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Fabric Resource Conditions");
    public static FeatureFlagSet currentFeatures = null;
    public static Exception DISABLED_RESOURCE_EXCEPTION = new Exception("Disabled resource");

    @Override
    public void onInitialize() {
        ResourceConditions.register(DefaultResourceConditionTypes.TRUE);
        ResourceConditions.register(DefaultResourceConditionTypes.NOT);
        ResourceConditions.register(DefaultResourceConditionTypes.AND);
        ResourceConditions.register(DefaultResourceConditionTypes.OR);
        ResourceConditions.register(DefaultResourceConditionTypes.ALL_MODS_LOADED);
        ResourceConditions.register(DefaultResourceConditionTypes.ANY_MODS_LOADED);
        ResourceConditions.register(DefaultResourceConditionTypes.TAGS_POPULATED);
        ResourceConditions.register(DefaultResourceConditionTypes.FEATURES_ENABLED);
        ResourceConditions.register(DefaultResourceConditionTypes.REGISTRY_CONTAINS);
    }

    public static boolean applyResourceConditions(JsonObject obj, String dataType, Identifier key,  @Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        boolean debugLogEnabled = LOGGER.isDebugEnabled();
        if (obj.has("fabric:load_conditions")) {
            DataResult conditions = ResourceCondition.CONDITION_CODEC.parse(JsonOps.INSTANCE, obj.get("fabric:load_conditions"));
            if (conditions.isSuccess()) {
                boolean matched = ((ResourceCondition)conditions.getOrThrow()).test(registryInfo);
                if (debugLogEnabled) {
                    String verdict = matched ? "Allowed" : "Rejected";
                    LOGGER.debug("{} resource of type {} with id {}", verdict, dataType, key);
                }
                return matched;
            }
            LOGGER.error("Failed to parse resource conditions for file of type {} with id {}, skipping: {}", dataType, key, conditions.error().get().message());
        }
        return true;
    }

    public static boolean conditionsMet(List<ResourceCondition> conditions,  @Nullable RegistryOps.RegistryInfoLookup registryInfo, boolean and) {
        for (ResourceCondition condition : conditions) {
            if (condition.test(registryInfo) == and) continue;
            return !and;
        }
        return and;
    }

    public static boolean modsLoaded(List<String> modIds, boolean and) {
        for (String modId : modIds) {
            if (FabricLoader.getInstance().isModLoaded(modId) == and) continue;
            return !and;
        }
        return and;
    }

    public static boolean tagsPopulated( @Nullable RegistryOps.RegistryInfoLookup infoGetter, Identifier registryId, List<Identifier> tags) {
        if (infoGetter == null) {
            LOGGER.warn("Can't retrieve registry {}, failing tags_populated resource condition check", (Object)registryId);
            return false;
        }
        ResourceKey registryKey = ResourceKey.createRegistryKey(registryId);
        Optional optionalInfo = infoGetter.lookup(registryKey);
        if (optionalInfo.isPresent()) {
            HolderGetter lookup = optionalInfo.get().getter();
            for (Identifier id : tags) {
                if (!lookup.get(TagKey.create(registryKey, id)).isEmpty()) continue;
                return false;
            }
            return true;
        }
        return tags.isEmpty();
    }

    public static boolean featuresEnabled(Collection<Identifier> features) {
        MutableBoolean foundUnknown = new MutableBoolean();
        FeatureFlagSet set = FeatureFlags.REGISTRY.fromNames(features, id -> {
            LOGGER.info("Found unknown feature {}, treating it as failure", id);
            foundUnknown.setTrue();
        });
        if (foundUnknown.booleanValue()) {
            return false;
        }
        if (currentFeatures == null) {
            LOGGER.warn("Can't retrieve current features, failing features_enabled resource condition check.");
            return false;
        }
        return set.isSubsetOf(currentFeatures);
    }

    public static boolean registryContains( @Nullable RegistryOps.RegistryInfoLookup infoGetter, Identifier registryId, List<Identifier> entries) {
        if (infoGetter == null) {
            LOGGER.warn("Can't retrieve registry {}, failing registry_contains resource condition check", (Object)registryId);
            return false;
        }
        ResourceKey registryKey = ResourceKey.createRegistryKey(registryId);
        Optional optionalInfo = infoGetter.lookup(registryKey);
        if (optionalInfo.isPresent()) {
            HolderGetter lookup = optionalInfo.get().getter();
            for (Identifier id : entries) {
                if (!lookup.get(ResourceKey.create(registryKey, id)).isEmpty()) continue;
                return false;
            }
            return true;
        }
        return entries.isEmpty();
    }
}

