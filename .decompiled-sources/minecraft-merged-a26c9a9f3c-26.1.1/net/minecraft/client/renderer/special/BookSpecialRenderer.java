/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class BookSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final SpriteGetter sprites;
    private final BookModel model;
    private final BookModel.State state;

    public BookSpecialRenderer(SpriteGetter sprites, BookModel model, BookModel.State state) {
        this.sprites = sprites;
        this.model = model;
        this.state = state;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        submitNodeCollector.submitModel(this.model, this.state, poseStack, lightCoords, overlayCoords, -1, EnchantTableRenderer.BOOK_TEXTURE, this.sprites, outlineColor, null);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.setupAnim(this.state);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(float openAngle, float page1, float page2) implements NoDataSpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("open_angle")).forGetter(Unbaked::openAngle), ((MapCodec)Codec.FLOAT.fieldOf("page1")).forGetter(Unbaked::page1), ((MapCodec)Codec.FLOAT.fieldOf("page2")).forGetter(Unbaked::page2)).apply((Applicative<Unbaked, ?>)i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public BookSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new BookSpecialRenderer(context.sprites(), new BookModel(context.entityModelSet().bakeLayer(ModelLayers.BOOK)), new BookModel.State(this.openAngle * ((float)Math.PI / 180), this.page1, this.page2));
        }
    }
}

