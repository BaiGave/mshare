/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag.convention.v2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Locale;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.tags.TagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslationConventionLogWarnings
implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationConventionLogWarnings.class);
    private static final LogWarningMode LOG_UNTRANSLATED_WARNING_MODE = TranslationConventionLogWarnings.setupLogWarningModeProperty();

    private static LogWarningMode setupLogWarningModeProperty() {
        LogWarningMode defaultMode = FabricLoader.getInstance().isDevelopmentEnvironment() ? LogWarningMode.SHORT : LogWarningMode.SILENCED;
        String property = System.getProperty("fabric-tag-conventions-v2.missingTagTranslationWarning", defaultMode.name()).toUpperCase(Locale.ROOT);
        try {
            return LogWarningMode.valueOf(property);
        }
        catch (Exception e) {
            LOGGER.error("Unknown entry `{}` for property `fabric-tag-conventions-v2.missingTagTranslationWarning`.", (Object)property);
            return LogWarningMode.SILENCED;
        }
    }

    @Override
    public void onInitialize() {
        if (LOG_UNTRANSLATED_WARNING_MODE != LogWarningMode.SILENCED) {
            TranslationConventionLogWarnings.setupUntranslatedItemTagWarning();
        }
    }

    private static void setupUntranslatedItemTagWarning() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Language language = Language.getInstance();
            HolderLookup.RegistryLookup itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM);
            ObjectArrayList untranslatedItemTags = new ObjectArrayList();
            itemRegistry.getTags().forEach(itemTagKey -> {
                if (itemTagKey.key().location().getNamespace().equals("minecraft")) {
                    return;
                }
                if (!language.has(itemTagKey.key().getTranslationKey())) {
                    untranslatedItemTags.add(itemTagKey.key());
                }
            });
            if (untranslatedItemTags.isEmpty()) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\tDev warning - Untranslated Item Tags detected. Please translate your item tags so other mods such as recipe viewers can properly display your tag's name.\n\tThe format desired is tag.item.<namespace>.<path> for the translation key with slashes in path turned into periods.\n\tTo disable this message, set this system property in your runs: `-Dfabric-tag-conventions-v2.missingTagTranslationWarning=SILENCED`.\n\tTo see individual untranslated item tags found, set the system property to `-Dfabric-tag-conventions-v2.missingTagTranslationWarning=VERBOSE`.\n\tDefault is `SHORT`.\n");
            if (LOG_UNTRANSLATED_WARNING_MODE.verbose()) {
                stringBuilder.append("\nUntranslated item tags:");
                for (TagKey tagKey : untranslatedItemTags) {
                    stringBuilder.append("\n     ").append(tagKey.location());
                }
            }
            LOGGER.warn(stringBuilder.toString());
            if (LOG_UNTRANSLATED_WARNING_MODE == LogWarningMode.FAIL) {
                throw new RuntimeException("Tag translation validation failed");
            }
        });
    }

    private static enum LogWarningMode {
        SILENCED,
        SHORT,
        VERBOSE,
        FAIL;


        boolean verbose() {
            return this == VERBOSE || this == FAIL;
        }
    }
}

