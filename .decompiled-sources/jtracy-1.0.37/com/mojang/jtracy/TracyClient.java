/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.jtracy;

import com.mojang.jtracy.ContinuousFrame;
import com.mojang.jtracy.DiscontinuousFrame;
import com.mojang.jtracy.GpuApi;
import com.mojang.jtracy.GpuContext;
import com.mojang.jtracy.Loader;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyBindings;
import com.mojang.jtracy.Zone;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TracyClient {
    private static boolean loaded = false;
    private static AtomicInteger lastGpuContextId = new AtomicInteger(0);

    public static boolean isAvailable() {
        return loaded;
    }

    public static synchronized void load() throws UnsatisfiedLinkError {
        if (!loaded) {
            new Loader().load();
            loaded = true;
        }
    }

    public static void markFrame() {
        if (loaded) {
            TracyBindings.markFrame(0L);
        }
    }

    public static void frameImage(ByteBuffer image, int width, int height, int offset, boolean flip) {
        if (loaded) {
            TracyBindings.frameImage(image, width, height, offset, flip);
        }
    }

    public static Zone beginZone(String name, boolean captureSource) {
        if (loaded) {
            StackWalker walker;
            Optional result;
            String function = "";
            String file = "";
            int line = 0;
            if (captureSource && (result = (walker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 2)).walk(s -> s.filter(frame -> frame.getDeclaringClass() != TracyClient.class).findFirst())).isPresent()) {
                StackWalker.StackFrame frame = (StackWalker.StackFrame)result.get();
                function = frame.getMethodName();
                file = frame.getFileName();
                line = frame.getLineNumber();
            }
            return new Zone(TracyBindings.beginZone(name, function, file, line));
        }
        return Zone.UNAVAILABLE;
    }

    public static Zone beginZone(String name, String function, String file, int line) {
        if (loaded) {
            return new Zone(TracyBindings.beginZone(name, function, file, line));
        }
        return Zone.UNAVAILABLE;
    }

    public static void setThreadName(String name, int group) {
        if (loaded) {
            TracyBindings.setThreadName(name, group);
        }
    }

    public static Plot createPlot(String name) {
        if (loaded) {
            return new Plot(TracyBindings.leakName(name));
        }
        return Plot.UNAVAILABLE;
    }

    public static DiscontinuousFrame createDiscontinuousFrame(String name) {
        if (loaded) {
            return new DiscontinuousFrame(TracyBindings.leakName(name));
        }
        return DiscontinuousFrame.UNAVAILABLE;
    }

    public static ContinuousFrame createContinuousFrame(String name) {
        if (loaded) {
            return new ContinuousFrame(TracyBindings.leakName(name));
        }
        return ContinuousFrame.UNAVAILABLE;
    }

    public static MemoryPool createMemoryPool(String name) {
        if (loaded) {
            return new MemoryPool(TracyBindings.leakName(name));
        }
        return MemoryPool.UNAVAILABLE;
    }

    public static void reportAppInfo(String text) {
        if (loaded) {
            TracyBindings.appInfo(text);
        }
    }

    public static void message(String text) {
        if (loaded) {
            TracyBindings.message(text);
        }
    }

    public static void message(String text, int color) {
        if (loaded) {
            TracyBindings.messageColored(text, color);
        }
    }

    public static void message(Supplier<String> text) {
        if (loaded) {
            TracyBindings.message(text.get());
        }
    }

    public static void message(Supplier<String> text, int color) {
        if (loaded) {
            TracyBindings.messageColored(text.get(), color);
        }
    }

    public static GpuContext createGpuContext(GpuApi api, long gpuTimestamp, float gpuPeriod) {
        if (loaded) {
            int id = lastGpuContextId.incrementAndGet();
            if (id == 255) {
                throw new UnsupportedOperationException("Too many GPU contexts were created");
            }
            TracyBindings.newGpuContext(id, gpuTimestamp, gpuPeriod, 0, api.getId());
            return new GpuContext(id);
        }
        return GpuContext.UNAVAILABLE;
    }
}

