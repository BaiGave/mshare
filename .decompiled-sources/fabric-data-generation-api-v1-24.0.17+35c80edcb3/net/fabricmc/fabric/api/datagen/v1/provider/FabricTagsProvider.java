/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public abstract class FabricTagsProvider<T>
extends TagsProvider<T> {
    private final FabricPackOutput output;
    private final Map<Identifier, AliasGroupBuilder> aliasGroupBuilders = new HashMap<Identifier, AliasGroupBuilder>();

    public FabricTagsProvider(FabricPackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryKey, registryLookupFuture);
        this.output = output;
    }

    @Override
    protected abstract void addTags(HolderLookup.Provider var1);

    protected TagAppender<ResourceKey<T>, T> builder(TagKey<T> tag) {
        TagBuilder tagBuilder = this.getOrCreateRawBuilder(tag);
        return TagAppender.forBuilder(tagBuilder);
    }

    protected AliasGroupBuilder aliasGroup(Identifier groupId) {
        return this.aliasGroupBuilders.computeIfAbsent(groupId, key -> new AliasGroupBuilder(this));
    }

    protected AliasGroupBuilder aliasGroup(String group) {
        Identifier groupId = Identifier.fromNamespaceAndPath(this.output.getModId(), group);
        return this.aliasGroupBuilders.computeIfAbsent(groupId, key -> new AliasGroupBuilder(this));
    }

    public Map<Identifier, AliasGroupBuilder> getAliasGroupBuilders() {
        return Collections.unmodifiableMap(this.aliasGroupBuilders);
    }

    public final class AliasGroupBuilder {
        private final List<TagKey<T>> tags;
        final /* synthetic */ FabricTagsProvider this$0;

        private AliasGroupBuilder(FabricTagsProvider this$0) {
            FabricTagsProvider fabricTagsProvider = this$0;
            Objects.requireNonNull(fabricTagsProvider);
            this.this$0 = fabricTagsProvider;
            this.tags = new ArrayList();
        }

        public List<TagKey<T>> getTags() {
            return Collections.unmodifiableList(this.tags);
        }

        public AliasGroupBuilder add(TagKey<T> tag) {
            if (tag.registry() != this.this$0.registryKey) {
                throw new IllegalArgumentException("Tag " + String.valueOf(tag) + " isn't from the registry " + String.valueOf(this.this$0.registryKey));
            }
            this.tags.add(tag);
            return this;
        }

        @SafeVarargs
        public final AliasGroupBuilder add(TagKey<T> ... tags) {
            for (TagKey tag : tags) {
                this.add((TagKey<T>)tag);
            }
            return this;
        }

        public AliasGroupBuilder add(Identifier tag) {
            this.tags.add(TagKey.create(this.this$0.registryKey, tag));
            return this;
        }

        public AliasGroupBuilder add(Identifier ... tags) {
            for (Identifier tag : tags) {
                this.tags.add(TagKey.create(this.this$0.registryKey, tag));
            }
            return this;
        }
    }

    public static abstract class EntityTypeTagsProvider
    extends FabricIntrinsicHolderTagsProvider<EntityType<?>> {
        public EntityTypeTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, Registries.ENTITY_TYPE, registryLookupFuture, (T type) -> type.builtInRegistryHolder().key());
        }
    }

    public static abstract class FluidTagsProvider
    extends FabricIntrinsicHolderTagsProvider<Fluid> {
        public FluidTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, Registries.FLUID, registryLookupFuture, (T fluid) -> fluid.builtInRegistryHolder().key());
        }
    }

    public static abstract class ItemTagsProvider
    extends FabricIntrinsicHolderTagsProvider<Item> {
        private final @Nullable Function<TagKey<Block>, TagBuilder> blockTagBuilderProvider;

        public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture, @Nullable BlockTagsProvider blockTagsProvider) {
            super(output, Registries.ITEM, registryLookupFuture, (T item) -> item.builtInRegistryHolder().key());
            Function<TagKey, TagBuilder> function;
            if (blockTagsProvider == null) {
                function = null;
            } else {
                BlockTagsProvider blockTagsProvider2 = blockTagsProvider;
                Objects.requireNonNull(blockTagsProvider2);
                BlockTagsProvider blockTagsProvider3 = blockTagsProvider2;
                function = x$0 -> ((FabricTagsProvider)blockTagsProvider3).getOrCreateRawBuilder(x$0);
            }
            this.blockTagBuilderProvider = function;
        }

        public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            this(output, registryLookupFuture, null);
        }

        public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
            TagBuilder blockTagBuilder = Objects.requireNonNull(this.blockTagBuilderProvider, "Pass Block tags provider via constructor to use copy").apply(blockTag);
            TagBuilder itemTagBuilder = this.getOrCreateRawBuilder(itemTag);
            blockTagBuilder.build().forEach(itemTagBuilder::add);
        }
    }

    public static abstract class BlockEntityTypeTagsProvider
    extends FabricIntrinsicHolderTagsProvider<BlockEntityType<?>> {
        public BlockEntityTypeTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, Registries.BLOCK_ENTITY_TYPE, registryLookupFuture, (T type) -> type.builtInRegistryHolder().key());
        }
    }

    public static abstract class BlockTagsProvider
    extends FabricIntrinsicHolderTagsProvider<Block> {
        public BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
            super(output, Registries.BLOCK, registryLookupFuture, (T block) -> block.builtInRegistryHolder().key());
        }
    }

    public static abstract class FabricIntrinsicHolderTagsProvider<T>
    extends FabricTagsProvider<T> {
        private final Function<T, ResourceKey<T>> valueToKey;

        protected FabricIntrinsicHolderTagsProvider(FabricPackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registryLookupFuture, Function<T, ResourceKey<T>> valueToKey) {
            super(output, registryKey, registryLookupFuture);
            this.valueToKey = valueToKey;
        }

        protected TagAppender<T, T> valueLookupBuilder(TagKey<T> tag) {
            TagBuilder tagBuilder = this.getOrCreateRawBuilder(tag);
            return TagAppender.forBuilder(tagBuilder).map(this.valueToKey);
        }
    }
}

