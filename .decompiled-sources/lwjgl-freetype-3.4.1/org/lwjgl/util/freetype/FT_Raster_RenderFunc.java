/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.util.freetype.FT_Raster_RenderFuncI;

public abstract class FT_Raster_RenderFunc
extends Callback
implements FT_Raster_RenderFuncI {
    public static FT_Raster_RenderFunc create(long functionPointer) {
        FT_Raster_RenderFuncI instance = (FT_Raster_RenderFuncI)Callback.get(functionPointer);
        return instance instanceof FT_Raster_RenderFunc ? (FT_Raster_RenderFunc)instance : new Container(functionPointer, instance);
    }

    public static @Nullable FT_Raster_RenderFunc createSafe(long functionPointer) {
        return functionPointer == 0L ? null : FT_Raster_RenderFunc.create(functionPointer);
    }

    public static FT_Raster_RenderFunc create(FT_Raster_RenderFuncI instance) {
        return instance instanceof FT_Raster_RenderFunc ? (FT_Raster_RenderFunc)instance : new Container(instance.address(), instance);
    }

    protected FT_Raster_RenderFunc() {
        super(DESCRIPTOR);
    }

    FT_Raster_RenderFunc(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends FT_Raster_RenderFunc {
        private final FT_Raster_RenderFuncI delegate;

        Container(long functionPointer, FT_Raster_RenderFuncI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public int invoke(long raster, long params) {
            return this.delegate.invoke(raster, params);
        }
    }
}

