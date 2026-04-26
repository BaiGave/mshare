/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class StandingSignSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final SpriteGetter sprites;
    private final Model.Simple model;
    private final SpriteId sprite;

    public StandingSignSpecialRenderer(SpriteGetter sprites, Model.Simple model, SpriteId sprite) {
        this.sprites = sprites;
        this.model = model;
        this.sprite = sprite;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        StandingSignRenderer.submitSpecial(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, this.sprite);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(WoodType woodType, PlainSignBlock.Attachment attachment, Optional<Identifier> texture) implements NoDataSpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WoodType.CODEC.fieldOf("wood_type")).forGetter(Unbaked::woodType), PlainSignBlock.Attachment.CODEC.optionalFieldOf("attachement", PlainSignBlock.Attachment.GROUND).forGetter(Unbaked::attachment), Identifier.CODEC.optionalFieldOf("texture").forGetter(Unbaked::texture)).apply((Applicative<Unbaked, ?>)i, Unbaked::new));

        public Unbaked(WoodType woodType, PlainSignBlock.Attachment attachment) {
            this(woodType, attachment, Optional.empty());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public StandingSignSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            Model.Simple model = StandingSignRenderer.createSignModel(context.entityModelSet(), this.woodType, this.attachment);
            SpriteId sprite = this.texture.map(Sheets.SIGN_MAPPER::apply).orElseGet(() -> Sheets.getSignSprite(this.woodType));
            return new StandingSignSpecialRenderer(context.sprites(), model, sprite);
        }
    }
}

