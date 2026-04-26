/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LootUtil {
    public static final ThreadLocal<Map<Identifier, LootTableSource>> SOURCES = ThreadLocal.withInitial(HashMap::new);

    public static LootTableSource determineSource(Resource resource) {
        if (resource != null) {
            PackSource packSource = resource.getFabricPackSource();
            if (packSource == PackSource.BUILT_IN) {
                return LootTableSource.VANILLA;
            }
            if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
                return LootTableSource.MOD;
            }
        }
        return LootTableSource.DATA_PACK;
    }

    public static Holder<LootTable> getEntryOrDirect(ServerLevel level, LootTable table) {
        HolderLookup.Provider provider = level.getServer().reloadableRegistries().lookup();
        HolderLookup lootTableHolderLookup = provider.lookup(Registries.LOOT_TABLE).orElseThrow(() -> new IllegalStateException("Failed to fetch LootTable provider from HolderLookup.Provider"));
        return lootTableHolderLookup.listElements().filter(it -> ((LootTable)it.value()).equals(table)).findFirst().map(Function.identity()).orElseGet(() -> Holder.direct(table));
    }
}

