/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.util.freetype.FT_Module_ConstructorI;

public abstract class FT_Module_Constructor
extends Callback
implements FT_Module_ConstructorI {
    public static FT_Module_Constructor create(long functionPointer) {
        FT_Module_ConstructorI instance = (FT_Module_ConstructorI)Callback.get(functionPointer);
        return instance instanceof FT_Module_Constructor ? (FT_Module_Constructor)instance : new Container(functionPointer, instance);
    }

    public static @Nullable FT_Module_Constructor createSafe(long functionPointer) {
        return functionPointer == 0L ? null : FT_Module_Constructor.create(functionPointer);
    }

    public static FT_Module_Constructor create(FT_Module_ConstructorI instance) {
        return instance instanceof FT_Module_Constructor ? (FT_Module_Constructor)instance : new Container(instance.address(), instance);
    }

    protected FT_Module_Constructor() {
        super(DESCRIPTOR);
    }

    FT_Module_Constructor(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends FT_Module_Constructor {
        private final FT_Module_ConstructorI delegate;

        Container(long functionPointer, FT_Module_ConstructorI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public int invoke(long module) {
            return this.delegate.invoke(module);
        }
    }
}

