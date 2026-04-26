/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.util.freetype.FT_Raster_SetModeFuncI;

public abstract class FT_Raster_SetModeFunc
extends Callback
implements FT_Raster_SetModeFuncI {
    public static FT_Raster_SetModeFunc create(long functionPointer) {
        FT_Raster_SetModeFuncI instance = (FT_Raster_SetModeFuncI)Callback.get(functionPointer);
        return instance instanceof FT_Raster_SetModeFunc ? (FT_Raster_SetModeFunc)instance : new Container(functionPointer, instance);
    }

    public static @Nullable FT_Raster_SetModeFunc createSafe(long functionPointer) {
        return functionPointer == 0L ? null : FT_Raster_SetModeFunc.create(functionPointer);
    }

    public static FT_Raster_SetModeFunc create(FT_Raster_SetModeFuncI instance) {
        return instance instanceof FT_Raster_SetModeFunc ? (FT_Raster_SetModeFunc)instance : new Container(instance.address(), instance);
    }

    protected FT_Raster_SetModeFunc() {
        super(DESCRIPTOR);
    }

    FT_Raster_SetModeFunc(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends FT_Raster_SetModeFunc {
        private final FT_Raster_SetModeFuncI delegate;

        Container(long functionPointer, FT_Raster_SetModeFuncI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public int invoke(long raster, long mode, long args) {
            return this.delegate.invoke(raster, mode, args);
        }
    }
}

