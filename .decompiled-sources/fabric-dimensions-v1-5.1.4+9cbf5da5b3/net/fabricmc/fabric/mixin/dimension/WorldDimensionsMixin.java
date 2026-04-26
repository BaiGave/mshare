/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.impl.dimension.FailSoftMapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={WorldDimensions.class})
public class WorldDimensionsMixin {
    @Redirect(method={"lambda$static$0"}, at=@At(value="INVOKE", target="Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;group(Lcom/mojang/datafixers/kinds/App;)Lcom/mojang/datafixers/Products$P1;"))
    private static Products.P1 useFailSoftMap(RecordCodecBuilder.Instance instance, App app) {
        return instance.group(((MapCodec)new FailSoftMapCodec<ResourceKey<LevelStem>, LevelStem>(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions")).forGetter(WorldDimensions::dimensions));
    }
}

