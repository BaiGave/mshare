/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.gametest.v1;

import java.lang.reflect.Method;
import net.minecraft.gametest.framework.GameTestHelper;

public interface CustomTestMethodInvoker {
    public void invokeTestMethod(GameTestHelper var1, Method var2) throws ReflectiveOperationException;
}

