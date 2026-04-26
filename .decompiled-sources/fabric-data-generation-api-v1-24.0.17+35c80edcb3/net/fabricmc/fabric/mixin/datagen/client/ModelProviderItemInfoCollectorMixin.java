/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.client.FabricItemAssetDefinitions;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ModelProvider.ItemInfoCollector.class})
public class ModelProviderItemInfoCollectorMixin
implements FabricItemAssetDefinitions {
    @Unique
    private FabricPackOutput fabricPackOutput;
    @Unique
    private Set<Block> processedBlocks;

    @Override
    public void fabric_setProcessedBlocks(Set<Block> processedBlocks) {
        this.processedBlocks = processedBlocks;
    }

    @Override
    public void setFabricPackOutput(FabricPackOutput fabricPackOutput) {
        this.fabricPackOutput = fabricPackOutput;
    }

    @WrapOperation(method={"lambda$finalizeAndValidate$0"}, at={@At(value="INVOKE", target="Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", ordinal=1)})
    private boolean filterItemsForProcessingMod(Map<Item, ClientItem> map, Object o, Operation<Boolean> original) {
        BlockItem blockItem = (BlockItem)o;
        if (this.fabricPackOutput != null) {
            if (!this.processedBlocks.contains(blockItem.getBlock())) {
                return true;
            }
            if (!BuiltInRegistries.ITEM.getKey(blockItem).getNamespace().equals(this.fabricPackOutput.getModId())) {
                return true;
            }
        }
        return original.call(map, o);
    }

    @ModifyArg(method={"finalizeAndValidate"}, at=@At(value="INVOKE", target="Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal=0))
    private Predicate<Holder.Reference<Item>> filterItemsForProcessingMod(Predicate<Holder.Reference<Item>> original) {
        if (this.fabricPackOutput != null) {
            return original.and(item -> this.fabricPackOutput.isStrictValidationEnabled()).and(item -> item.key().identifier().getNamespace().equals(this.fabricPackOutput.getModId()));
        }
        return original;
    }
}

