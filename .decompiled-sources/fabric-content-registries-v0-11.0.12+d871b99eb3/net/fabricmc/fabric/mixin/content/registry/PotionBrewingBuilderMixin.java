/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.List;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PotionBrewing.Builder.class})
public abstract class PotionBrewingBuilderMixin
implements FabricPotionBrewingBuilder {
    @Shadow
    @Final
    private FeatureFlagSet enabledFeatures;
    @Shadow
    @Final
    private List<PotionBrewing.Mix<Item>> containerMixes;
    @Shadow
    @Final
    private List<PotionBrewing.Mix<Potion>> potionMixes;

    @Shadow
    private static void expectPotion(Item potionType) {
    }

    @Inject(method={"build"}, at={@At(value="HEAD")})
    private void build(CallbackInfoReturnable<PotionBrewing> cir) {
        FabricPotionBrewingBuilder.BUILD.invoker().build((PotionBrewing.Builder)((Object)this));
    }

    @Override
    public void registerItemRecipe(Item input, Ingredient ingredient, Item output) {
        if (input.isEnabled(this.enabledFeatures) && output.isEnabled(this.enabledFeatures)) {
            PotionBrewingBuilderMixin.expectPotion(input);
            PotionBrewingBuilderMixin.expectPotion(output);
            this.containerMixes.add(new PotionBrewing.Mix<Item>(input.builtInRegistryHolder(), ingredient, output.builtInRegistryHolder()));
        }
    }

    @Override
    public void registerPotionRecipe(Holder<Potion> input, Ingredient ingredient, Holder<Potion> output) {
        if (input.value().isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
            this.potionMixes.add(new PotionBrewing.Mix<Potion>(input, ingredient, output));
        }
    }

    @Override
    public void registerRecipes(Ingredient ingredient, Holder<Potion> potion) {
        if (potion.value().isEnabled(this.enabledFeatures)) {
            this.registerPotionRecipe(Potions.WATER, ingredient, Potions.MUNDANE);
            this.registerPotionRecipe(Potions.AWKWARD, ingredient, potion);
        }
    }

    @Override
    public FeatureFlagSet getEnabledFeatures() {
        return this.enabledFeatures;
    }
}

