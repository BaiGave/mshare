/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@FunctionalInterface
interface ModCandidateFinder {
    public void findCandidates(ModCandidateConsumer var1);

    public static interface ModCandidateConsumer {
        default public void accept(Path path, boolean requiresRemap) {
            this.accept(Collections.singletonList(path), requiresRemap);
        }

        public void accept(List<Path> var1, boolean var2);
    }
}

