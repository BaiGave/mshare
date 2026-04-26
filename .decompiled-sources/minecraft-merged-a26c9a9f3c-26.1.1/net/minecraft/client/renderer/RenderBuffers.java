/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int maxSectionBuilders) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(maxSectionBuilders);
        SequencedMap fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap(), map -> {
            map.put(Sheets.cutoutBlockItemSheet(), this.fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
            map.put(Sheets.translucentBlockItemSheet(), this.fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT));
            RenderBuffers.put(map, Sheets.cutoutItemSheet());
            RenderBuffers.put(map, Sheets.translucentItemSheet());
            RenderBuffers.put(map, RenderTypes.armorEntityGlint());
            RenderBuffers.put(map, RenderTypes.glint());
            RenderBuffers.put(map, RenderTypes.glintTranslucent());
            RenderBuffers.put(map, RenderTypes.entityGlint());
            RenderBuffers.put(map, RenderTypes.waterMask());
        });
        this.bufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, new ByteBufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource();
        SequencedMap crumblingBuffers = Util.make(new Object2ObjectLinkedOpenHashMap(), map -> ModelBakery.DESTROY_TYPES.forEach(type -> RenderBuffers.put(map, type)));
        this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers(crumblingBuffers, new ByteBufferBuilder(0));
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType type) {
        map.put(type, new ByteBufferBuilder(type.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}

