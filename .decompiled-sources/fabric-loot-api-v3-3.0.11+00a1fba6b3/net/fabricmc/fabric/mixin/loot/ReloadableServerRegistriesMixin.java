/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.loot.FabricLootTable;
import net.fabricmc.fabric.impl.loot.LootUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.Validatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ReloadableServerRegistries.class})
abstract class ReloadableServerRegistriesMixin {
    @Unique
    private static final WeakHashMap<RegistryOps<JsonElement>, HolderLookup.Provider> WRAPPERS = new WeakHashMap();

    ReloadableServerRegistriesMixin() {
    }

    @WrapOperation(method={"reload"}, at={@At(value="INVOKE", target="Lnet/minecraft/core/HolderLookup$Provider;createSerializationContext(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/resources/RegistryOps;")})
    private static RegistryOps<JsonElement> storeOps(HolderLookup.Provider holder, DynamicOps<JsonElement> ops, Operation<RegistryOps<JsonElement>> original) {
        RegistryOps<JsonElement> created = original.call(holder, ops);
        WRAPPERS.put(created, holder);
        return created;
    }

    @WrapOperation(method={"reload"}, at={@At(value="INVOKE", target="Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")})
    private static CompletableFuture<LayeredRegistryAccess<RegistryLayer>> removeOps(CompletableFuture<List<WritableRegistry<?>>> future, Function<? super List<WritableRegistry<?>>, ? extends LayeredRegistryAccess<RegistryLayer>> fn, Executor executor, Operation<CompletableFuture<LayeredRegistryAccess<RegistryLayer>>> original, @Local(name={"ops"}) RegistryOps<JsonElement> ops) {
        return original.call(future.thenApply(v -> {
            WRAPPERS.remove(ops);
            return v;
        }), fn, executor);
    }

    @Inject(method={"lambda$scheduleRegistryLoad$0"}, at={@At(value="INVOKE", target="Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V")})
    private static <T extends Validatable> void modifyLootTable(LootDataType<T> lootDataType, ResourceManager resourceManager, RegistryOps<JsonElement> registryOps, CallbackInfoReturnable<WritableRegistry<?>> cir, @Local(name={"elements"}) Map<Identifier, T> elements) {
        elements.replaceAll((identifier, t) -> ReloadableServerRegistriesMixin.modifyLootTable(t, identifier, registryOps));
    }

    @Unique
    private static <T> T modifyLootTable(T value, Identifier id, RegistryOps<JsonElement> ops) {
        if (!(value instanceof LootTable)) {
            return value;
        }
        LootTable table = (LootTable)value;
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, id);
        HolderLookup.Provider provider = WRAPPERS.get(ops);
        LootTableSource source = LootUtil.SOURCES.get().getOrDefault(id, LootTableSource.DATA_PACK);
        LootTable replacement = LootTableEvents.REPLACE.invoker().replaceLootTable(key, table, source, provider);
        if (replacement != null) {
            table = replacement;
            source = LootTableSource.REPLACED;
        }
        LootTable.Builder builder = FabricLootTableBuilder.copyOf(table);
        LootTableEvents.MODIFY.invoker().modifyLootTable(key, builder, source, provider);
        return (T)builder.build();
    }

    @Inject(method={"lambda$scheduleRegistryLoad$0"}, at={@At(value="RETURN")})
    private static <T extends Validatable> void onLootTablesLoaded(LootDataType<T> lootDataType, ResourceManager resourceManager, RegistryOps<JsonElement> registryOps, CallbackInfoReturnable<WritableRegistry<?>> cir) {
        if (lootDataType != LootDataType.TABLE) {
            return;
        }
        Registry lootTableRegistry = cir.getReturnValue();
        LootTableEvents.ALL_LOADED.invoker().onLootTablesLoaded(resourceManager, lootTableRegistry);
        LootUtil.SOURCES.remove();
        lootTableRegistry.listElements().forEach(reference -> ((FabricLootTable)reference.value()).fabric$setHolder((Holder<LootTable>)reference));
    }
}

