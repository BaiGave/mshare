/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.SkipPacketDecoderException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record ClientboundRecipeSyncPayload(List<Entry> entries) implements CustomPacketPayload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeSyncPayload> CODEC = Entry.CODEC.apply(ByteBufCodecs.list()).map(ClientboundRecipeSyncPayload::new, ClientboundRecipeSyncPayload::entries);
    public static final CustomPacketPayload.Type<ClientboundRecipeSyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("fabric", "recipe_sync"));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(RecipeSerializer<?> serializer, List<RecipeHolder<?>> recipes) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.ofMember(Entry::write, Entry::read);

        private static Entry read(RegistryFriendlyByteBuf buf) {
            Identifier recipeSerializerId = buf.readIdentifier();
            RecipeSerializer<?> recipeSerializer = BuiltInRegistries.RECIPE_SERIALIZER.getValue(recipeSerializerId);
            if (recipeSerializer == null || !RecipeSyncImpl.isSynced(recipeSerializer)) {
                throw new SkipPacketDecoderException("Tried syncing unsupported packet serializer '" + String.valueOf(recipeSerializerId) + "'!");
            }
            int count = buf.readVarInt();
            ArrayList list = new ArrayList();
            for (int i = 0; i < count; ++i) {
                ResourceKey<Recipe<?>> id = buf.readResourceKey(Registries.RECIPE);
                Recipe recipe = (Recipe)recipeSerializer.streamCodec().decode(buf);
                list.add(new RecipeHolder<Recipe>(id, recipe));
            }
            return new Entry(recipeSerializer, list);
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeIdentifier(BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.serializer));
            buf.writeVarInt(this.recipes.size());
            StreamCodec<RegistryFriendlyByteBuf, ?> serializer = this.serializer.streamCodec();
            for (RecipeHolder<?> recipe : this.recipes) {
                buf.writeResourceKey(recipe.id());
                serializer.encode(buf, recipe.value());
            }
        }
    }
}

