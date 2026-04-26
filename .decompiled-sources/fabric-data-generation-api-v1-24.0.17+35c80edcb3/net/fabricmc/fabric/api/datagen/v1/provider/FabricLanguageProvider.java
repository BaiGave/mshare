/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

public abstract class FabricLanguageProvider
implements DataProvider {
    protected final FabricPackOutput packOutput;
    private final String languageCode;
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    protected FabricLanguageProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        this(packOutput, "en_us", registryLookup);
    }

    protected FabricLanguageProvider(FabricPackOutput packOutput, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
        this.packOutput = packOutput;
        this.languageCode = languageCode;
        this.registryLookup = registryLookup;
    }

    public abstract void generateTranslations(HolderLookup.Provider var1, TranslationBuilder var2);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        TreeMap translationEntries = new TreeMap();
        return this.registryLookup.thenCompose(lookup -> {
            this.generateTranslations((HolderLookup.Provider)lookup, (key, value) -> {
                Objects.requireNonNull(key);
                Objects.requireNonNull(value);
                if (translationEntries.containsKey(key)) {
                    throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
                }
                translationEntries.put(key, value);
            });
            JsonObject langEntryJson = new JsonObject();
            for (Map.Entry entry : translationEntries.entrySet()) {
                langEntryJson.addProperty((String)entry.getKey(), (String)entry.getValue());
            }
            return DataProvider.saveStable(output, langEntryJson, this.getLangFilePath(this.languageCode));
        });
    }

    protected Path getLangFilePath(String code) {
        return this.packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang").json(Identifier.fromNamespaceAndPath(this.packOutput.getModId(), code));
    }

    @Override
    public String getName() {
        return "Language (%s)".formatted(this.languageCode);
    }

    @FunctionalInterface
    @ApiStatus.NonExtendable
    public static interface TranslationBuilder {
        public void add(String var1, String var2);

        default public void add(Item item, String value) {
            this.add(item.getDescriptionId(), value);
        }

        default public void add(Block block, String value) {
            this.add(block.getDescriptionId(), value);
        }

        default public void add(ResourceKey<CreativeModeTab> resourceKey, String value) {
            CreativeModeTab group = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(resourceKey);
            ComponentContents content = group.getDisplayName().getContents();
            if (content instanceof TranslatableContents) {
                TranslatableContents translatableContent = (TranslatableContents)content;
                this.add(translatableContent.getKey(), value);
                return;
            }
            throw new UnsupportedOperationException("Cannot add language entry for CreativeModeTab (%s) as the display name is not translatable.".formatted(group.getDisplayName().getString()));
        }

        default public void add(EntityType<?> entityType, String value) {
            this.add(entityType.getDescriptionId(), value);
        }

        default public void addEnchantment(ResourceKey<Enchantment> enchantment, String value) {
            this.add(Util.makeDescriptionId("enchantment", enchantment.identifier()), value);
        }

        default public void add(Holder<Attribute> attribute, String value) {
            this.add(attribute.value().getDescriptionId(), value);
        }

        default public void add(StatType<?> statType, String value) {
            this.add("stat_type." + BuiltInRegistries.STAT_TYPE.getKey(statType).toString().replace(':', '.'), value);
        }

        default public void add(MobEffect mobEffect, String value) {
            this.add(mobEffect.getDescriptionId(), value);
        }

        default public void add(Identifier identifier, String value) {
            this.add(identifier.toLanguageKey(), value);
        }

        default public void add(TagKey<?> tagKey, String value) {
            this.add(tagKey.getTranslationKey(), value);
        }

        default public void add(SoundEvent sound, String value) {
            this.add(Util.makeDescriptionId("subtitles", sound.location()), value);
        }

        default public void add(Path existingLanguageFile) throws IOException {
            try (BufferedReader reader = Files.newBufferedReader(existingLanguageFile);){
                JsonObject translations = StrictJsonParser.parse(reader).getAsJsonObject();
                for (String key : translations.keySet()) {
                    this.add(key, translations.get(key).getAsString());
                }
            }
        }
    }
}

