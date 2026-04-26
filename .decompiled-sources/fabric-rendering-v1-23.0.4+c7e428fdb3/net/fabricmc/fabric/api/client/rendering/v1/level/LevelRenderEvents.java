/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.level;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class LevelRenderEvents {
    public static final Event<AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = EventFactory.createArrayBacked(AfterBlockOutlineExtraction.class, callbacks -> (context, hit) -> {
        for (AfterBlockOutlineExtraction callback : callbacks) {
            callback.afterBlockOutlineExtraction(context, hit);
        }
    });
    public static final Event<EndExtraction> END_EXTRACTION = EventFactory.createArrayBacked(EndExtraction.class, callbacks -> context -> {
        for (EndExtraction callback : callbacks) {
            callback.endExtraction(context);
        }
    });
    public static final Event<StartMain> START_MAIN = EventFactory.createArrayBacked(StartMain.class, callbacks -> context -> {
        for (StartMain callback : callbacks) {
            callback.startMain(context);
        }
    });
    public static final Event<AfterOpaqueTerrain> AFTER_OPAQUE_TERRAIN = EventFactory.createArrayBacked(AfterOpaqueTerrain.class, callbacks -> context -> {
        for (AfterOpaqueTerrain callback : callbacks) {
            callback.afterOpaqueTerrain(context);
        }
    });
    public static final Event<CollectSubmits> COLLECT_SUBMITS = EventFactory.createArrayBacked(CollectSubmits.class, callbacks -> context -> {
        for (CollectSubmits callback : callbacks) {
            callback.collectSubmits(context);
        }
    });
    public static final Event<AfterSolidFeatures> AFTER_SOLID_FEATURES = EventFactory.createArrayBacked(AfterSolidFeatures.class, callbacks -> context -> {
        for (AfterSolidFeatures callback : callbacks) {
            callback.afterSolidFeatures(context);
        }
    });
    public static final Event<AfterTranslucentFeatures> AFTER_TRANSLUCENT_FEATURES = EventFactory.createArrayBacked(AfterTranslucentFeatures.class, callbacks -> context -> {
        for (AfterTranslucentFeatures callback : callbacks) {
            callback.afterTranslucentFeatures(context);
        }
    });
    public static final Event<BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = EventFactory.createArrayBacked(BeforeBlockOutline.class, callbacks -> (context, outlineRenderState) -> {
        boolean shouldRender = true;
        for (BeforeBlockOutline callback : callbacks) {
            if (callback.beforeBlockOutline(context, outlineRenderState)) continue;
            shouldRender = false;
        }
        return shouldRender;
    });
    public static final Event<BeforeGizmos> BEFORE_GIZMOS = EventFactory.createArrayBacked(BeforeGizmos.class, callbacks -> context -> {
        for (BeforeGizmos callback : callbacks) {
            callback.beforeGizmos(context);
        }
    });
    public static final Event<BeforeTranslucentTerrain> BEFORE_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(BeforeTranslucentTerrain.class, callbacks -> context -> {
        for (BeforeTranslucentTerrain callback : callbacks) {
            callback.beforeTranslucentTerrain(context);
        }
    });
    public static final Event<AfterTranslucentTerrain> AFTER_TRANSLUCENT_TERRAIN = EventFactory.createArrayBacked(AfterTranslucentTerrain.class, callbacks -> context -> {
        for (AfterTranslucentTerrain callback : callbacks) {
            callback.afterTranslucentTerrain(context);
        }
    });
    public static final Event<EndMain> END_MAIN = EventFactory.createArrayBacked(EndMain.class, callbacks -> context -> {
        for (EndMain callback : callbacks) {
            callback.endMain(context);
        }
    });

    private LevelRenderEvents() {
    }

    @FunctionalInterface
    public static interface EndMain {
        public void endMain(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface AfterTranslucentTerrain {
        public void afterTranslucentTerrain(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface BeforeTranslucentTerrain {
        public void beforeTranslucentTerrain(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface BeforeGizmos {
        public void beforeGizmos(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface BeforeBlockOutline {
        public boolean beforeBlockOutline(LevelRenderContext var1, BlockOutlineRenderState var2);
    }

    @FunctionalInterface
    public static interface AfterTranslucentFeatures {
        public void afterTranslucentFeatures(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface AfterSolidFeatures {
        public void afterSolidFeatures(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface CollectSubmits {
        public void collectSubmits(LevelRenderContext var1);
    }

    @FunctionalInterface
    public static interface AfterOpaqueTerrain {
        public void afterOpaqueTerrain(LevelTerrainRenderContext var1);
    }

    @FunctionalInterface
    public static interface StartMain {
        public void startMain(LevelTerrainRenderContext var1);
    }

    @FunctionalInterface
    public static interface EndExtraction {
        public void endExtraction(LevelExtractionContext var1);
    }

    @FunctionalInterface
    public static interface AfterBlockOutlineExtraction {
        public void afterBlockOutlineExtraction(LevelExtractionContext var1, @Nullable HitResult var2);
    }
}

