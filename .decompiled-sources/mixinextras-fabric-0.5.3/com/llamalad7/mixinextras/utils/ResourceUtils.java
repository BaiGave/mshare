/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.utils;

import java.io.InputStream;
import org.spongepowered.asm.service.MixinService;

class ResourceUtils {
    ResourceUtils() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static InputStream getResourceAsStream(String name) {
        InputStream result = MixinService.getService().getResourceAsStream(name);
        if (result != null) {
            return result;
        }
        ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ResourceUtils.class.getClassLoader());
        try {
            InputStream inputStream = MixinService.getService().getResourceAsStream(name);
            return inputStream;
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTccl);
        }
    }
}

