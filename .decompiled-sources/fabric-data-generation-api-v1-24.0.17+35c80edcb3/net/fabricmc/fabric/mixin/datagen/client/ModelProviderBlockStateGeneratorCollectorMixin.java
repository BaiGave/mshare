/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.client;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.client.FabricModelProviderDefinitions;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ModelProvider.BlockStateGeneratorCollector.class})
public class ModelProviderBlockStateGeneratorCollectorMixin
implements FabricModelProviderDefinitions {
    @Unique
    private FabricPackOutput fabricPackOutput;

    @Override
    public void setFabricPackOutput(FabricPackOutput fabricPackOutput) {
        this.fabricPackOutput = fabricPackOutput;
    }

    @ModifyArg(method={"validate"}, at=@At(value="INVOKE", target="Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal=0))
    private Predicate<Holder.Reference<Block>> filterBlocksForProcessingMod(Predicate<Holder.Reference<Block>> original) {
        if (this.fabricPackOutput != null) {
            return original.and(block -> this.fabricPackOutput.isStrictValidationEnabled()).and(block -> block.key().identifier().getNamespace().equals(this.fabricPackOutput.getModId()));
        }
        return original;
    }
}

