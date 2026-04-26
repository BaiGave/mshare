/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Arrays;
import java.util.Map;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;

enum ItemRenderType {
    CUTOUT(Sheets.cutoutItemSheet()),
    TRANSLUCENT(Sheets.translucentItemSheet()),
    CUTOUT_BLOCK(Sheets.cutoutBlockItemSheet()),
    TRANSLUCENT_BLOCK(Sheets.translucentBlockItemSheet());

    static final RenderType[] RENDER_TYPES;
    static final Map<RenderType, ItemRenderType> RENDER_TYPE_2_ENUM;
    static final ItemRenderType DEFAULT;
    final RenderType renderType;

    private ItemRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    static {
        RENDER_TYPES = (RenderType[])Arrays.stream(ItemRenderType.values()).map(t -> t.renderType).toArray(RenderType[]::new);
        RENDER_TYPE_2_ENUM = Map.of(ItemRenderType.CUTOUT.renderType, CUTOUT, ItemRenderType.TRANSLUCENT.renderType, TRANSLUCENT, ItemRenderType.CUTOUT_BLOCK.renderType, CUTOUT_BLOCK, ItemRenderType.TRANSLUCENT_BLOCK.renderType, TRANSLUCENT_BLOCK);
        DEFAULT = CUTOUT_BLOCK;
    }
}

