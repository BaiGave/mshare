/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.util.RegistryContextSwapper;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ClientItem(ItemModel.Unbaked model, Properties properties, @Nullable RegistryContextSwapper registrySwapper) {
    public static final Codec<ClientItem> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)ItemModels.CODEC.fieldOf("model")).forGetter(ClientItem::model), Properties.MAP_CODEC.forGetter(ClientItem::properties)).apply((Applicative<ClientItem, ?>)i, ClientItem::new));

    public ClientItem(ItemModel.Unbaked model, Properties properties) {
        this(model, properties, null);
    }

    public ClientItem withRegistrySwapper(RegistryContextSwapper registrySwapper) {
        return new ClientItem(this.model, this.properties, registrySwapper);
    }

    @Environment(value=EnvType.CLIENT)
    public record Properties(boolean handAnimationOnSwap, boolean oversizedInGui, float swapAnimationScale) {
        public static final Properties DEFAULT = new Properties(true, false, 1.0f);
        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("hand_animation_on_swap", true).forGetter(Properties::handAnimationOnSwap), Codec.BOOL.optionalFieldOf("oversized_in_gui", false).forGetter(Properties::oversizedInGui), Codec.FLOAT.optionalFieldOf("swap_animation_scale", Float.valueOf(1.0f)).forGetter(Properties::swapAnimationScale)).apply((Applicative<Properties, ?>)i, Properties::new));
    }
}

