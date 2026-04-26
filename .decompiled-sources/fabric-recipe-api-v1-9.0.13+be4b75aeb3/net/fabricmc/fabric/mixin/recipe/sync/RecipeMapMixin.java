/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.sync;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwarePreparedRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RecipeMap.class})
public class RecipeMapMixin
implements SyncedSerializerAwarePreparedRecipe {
    @Unique
    private Map<RecipeSerializer<?>, List<RecipeHolder<?>>> bySyncedSerializer;

    @Inject(method={"create"}, at={@At(value="HEAD")})
    private static void provideSerializerMap(Iterable<RecipeHolder<?>> recipes, CallbackInfoReturnable<RecipeMap> cir, @Share(value="bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeHolder<?>>>> bySerializer) {
        IdentityHashMap map = new IdentityHashMap();
        for (RecipeSerializer<?> serializer : RecipeSyncImpl.getSyncedSerializers()) {
            map.put(serializer, new ArrayList());
        }
        bySerializer.set(map);
    }

    @Inject(method={"create"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;")})
    private static void fillSerializerMap(Iterable<RecipeHolder<?>> recipes, CallbackInfoReturnable<RecipeMap> cir, @Local(name={"recipe"}) RecipeHolder<?> recipe, @Share(value="bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeHolder<?>>>> bySerializer) {
        List<RecipeHolder<?>> list = bySerializer.get().get(recipe.value().getSerializer());
        if (list != null) {
            list.add(recipe);
        }
    }

    @ModifyReturnValue(method={"create"}, at={@At(value="RETURN")})
    private static RecipeMap attachSerializerMap(RecipeMap original, @Share(value="bySerializer") LocalRef<IdentityHashMap<RecipeSerializer<?>, List<RecipeHolder<?>>>> bySerializer) {
        ((RecipeMapMixin)((Object)original)).bySyncedSerializer = bySerializer.get();
        return original;
    }

    @Override
    public @Nullable List<RecipeHolder<?>> fabric_getRecipesBySyncedSerializer(RecipeSerializer<?> serializer) {
        return this.bySyncedSerializer.get(serializer);
    }
}

