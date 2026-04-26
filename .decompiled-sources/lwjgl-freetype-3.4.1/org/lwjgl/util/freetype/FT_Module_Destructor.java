/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Callback;
import org.lwjgl.util.freetype.FT_Module_DestructorI;

public abstract class FT_Module_Destructor
extends Callback
implements FT_Module_DestructorI {
    public static FT_Module_Destructor create(long functionPointer) {
        FT_Module_DestructorI instance = (FT_Module_DestructorI)Callback.get(functionPointer);
        return instance instanceof FT_Module_Destructor ? (FT_Module_Destructor)instance : new Container(functionPointer, instance);
    }

    public static @Nullable FT_Module_Destructor createSafe(long functionPointer) {
        return functionPointer == 0L ? null : FT_Module_Destructor.create(functionPointer);
    }

    public static FT_Module_Destructor create(FT_Module_DestructorI instance) {
        return instance instanceof FT_Module_Destructor ? (FT_Module_Destructor)instance : new Container(instance.address(), instance);
    }

    protected FT_Module_Destructor() {
        super(DESCRIPTOR);
    }

    FT_Module_Destructor(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends FT_Module_Destructor {
        private final FT_Module_DestructorI delegate;

        Container(long functionPointer, FT_Module_DestructorI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(long module) {
            this.delegate.invoke(module);
        }
    }
}

