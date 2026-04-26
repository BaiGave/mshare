/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.waypoints;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import net.minecraft.world.waypoints.WaypointStyleAssets;

public interface Waypoint {
    public static final int MAX_RANGE = 60000000;
    public static final AttributeModifier WAYPOINT_TRANSMIT_RANGE_HIDE_MODIFIER = new AttributeModifier(Identifier.withDefaultNamespace("waypoint_transmit_range_hide"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    public static Item.Properties addHideAttribute(Item.Properties properties) {
        return properties.component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder().add(Attributes.WAYPOINT_TRANSMIT_RANGE, WAYPOINT_TRANSMIT_RANGE_HIDE_MODIFIER, EquipmentSlotGroup.HEAD, ItemAttributeModifiers.Display.hidden()).build());
    }

    public static class Icon {
        public static final Codec<Icon> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)ResourceKey.codec(WaypointStyleAssets.ROOT_ID).fieldOf("style")).forGetter(icon -> icon.style), ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color").forGetter(icon -> icon.color)).apply((Applicative<Icon, ?>)i, Icon::new));
        public static final StreamCodec<ByteBuf, Icon> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(WaypointStyleAssets.ROOT_ID), icon -> icon.style, ByteBufCodecs.optional(ByteBufCodecs.RGB_COLOR), icon -> icon.color, Icon::new);
        public static final Icon NULL = new Icon();
        public ResourceKey<WaypointStyleAsset> style = WaypointStyleAssets.DEFAULT;
        public Optional<Integer> color = Optional.empty();

        public Icon() {
        }

        private Icon(ResourceKey<WaypointStyleAsset> style, Optional<Integer> color) {
            this.style = style;
            this.color = color;
        }

        public boolean hasData() {
            return this.style != WaypointStyleAssets.DEFAULT || this.color.isPresent();
        }

        public Icon cloneAndAssignStyle(LivingEntity livingEntity) {
            ResourceKey<WaypointStyleAsset> overrideStyle = this.getOverrideStyle();
            Optional<Integer> colorOverride = this.color.or(() -> Optional.ofNullable(livingEntity.getTeam()).map(t -> t.getColor().getColor()).map(teamColor -> teamColor == 0 ? -13619152 : teamColor));
            if (overrideStyle == this.style && colorOverride.isEmpty()) {
                return this;
            }
            return new Icon(overrideStyle, colorOverride);
        }

        public void copyFrom(Icon other) {
            this.color = other.color;
            this.style = other.style;
        }

        private ResourceKey<WaypointStyleAsset> getOverrideStyle() {
            return this.style != WaypointStyleAssets.DEFAULT ? this.style : WaypointStyleAssets.DEFAULT;
        }
    }
}

