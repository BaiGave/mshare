/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.text2speech;

import com.mojang.text2speech.Narrator;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NarratorLinux
implements Narrator {
    private final AtomicInteger executionBatch = new AtomicInteger();
    private final Pointer voiceCmuUsKal16;
    private final ExecutorService executor;

    public NarratorLinux() throws Narrator.InitializeException {
        FliteLibrary.loadNative();
        FliteLibrary.CmuUsKal16.loadNative();
        int rc = FliteLibrary.flite_init();
        if (rc != 0) {
            throw new Narrator.InitializeException("flite returned code " + rc);
        }
        this.voiceCmuUsKal16 = FliteLibrary.CmuUsKal16.register_cmu_us_kal16(null);
        if (this.voiceCmuUsKal16 == Pointer.NULL) {
            throw new Narrator.InitializeException("flite_cmu_us_kal16 failed to register");
        }
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void say(String msg, boolean interrupt, float volume) {
        if (interrupt) {
            this.clear();
        }
        int thisBatch = this.executionBatch.get();
        Arrays.stream(msg.split("[,.:;/\"()\\[\\]{}!?\\\\]+")).filter(x -> !x.isBlank()).forEach(unit -> this.executor.submit(() -> {
            if (thisBatch < this.executionBatch.get()) {
                return;
            }
            Pointer utterance = FliteLibrary.flite_synth_text(unit, this.voiceCmuUsKal16);
            try {
                Pointer wave = FliteLibrary.utt_wave(utterance);
                if (volume != 1.0f) {
                    int volumeFactor = (int)(volume * 65536.0f);
                    FliteLibrary.cst_wave_rescale(wave, volumeFactor);
                }
                FliteLibrary.play_wave(wave);
            }
            finally {
                FliteLibrary.delete_utterance(utterance);
            }
        }));
    }

    @Override
    public void clear() {
        this.executionBatch.incrementAndGet();
    }

    @Override
    public void destroy() {
        this.executor.shutdownNow();
    }

    private static class FliteLibrary {
        private static final int SUCCESS = 0;
        private static final String NATIVE_LIBRARY_NAME = "flite";

        private FliteLibrary() {
        }

        public static void loadNative() throws Narrator.InitializeException {
            try {
                Native.register(FliteLibrary.class, NativeLibrary.getInstance(NATIVE_LIBRARY_NAME));
            }
            catch (Throwable e) {
                throw new Narrator.InitializeException("Failed to load library flite", e);
            }
        }

        private static native int flite_init();

        private static native Pointer flite_synth_text(String var0, Pointer var1);

        private static native Pointer utt_wave(Pointer var0);

        private static native void play_wave(Pointer var0);

        private static native void cst_wave_rescale(Pointer var0, int var1);

        private static native void delete_utterance(Pointer var0);

        private static class CmuUsKal16 {
            private static final String NATIVE_LIBRARY_NAME = "flite_cmu_us_kal16";

            private CmuUsKal16() {
            }

            public static void loadNative() throws Narrator.InitializeException {
                try {
                    Native.register(CmuUsKal16.class, NativeLibrary.getInstance(NATIVE_LIBRARY_NAME));
                }
                catch (Throwable e) {
                    throw new Narrator.InitializeException("Failed to load library flite_cmu_us_kal16", e);
                }
            }

            private static native Pointer register_cmu_us_kal16(String var0);
        }
    }
}

