/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.loader.impl.lib.tinyremapper.MetaInfFixer;
import net.fabricmc.loader.impl.lib.tinyremapper.MetaInfRemover;
import net.fabricmc.loader.impl.lib.tinyremapper.OutputConsumerPath;

public enum NonClassCopyMode {
    UNCHANGED(new OutputConsumerPath.ResourceRemapper[0]),
    FIX_META_INF(MetaInfFixer.INSTANCE),
    SKIP_META_INF(MetaInfRemover.INSTANCE);

    public final List<OutputConsumerPath.ResourceRemapper> remappers;

    private NonClassCopyMode(OutputConsumerPath.ResourceRemapper ... remappers) {
        this.remappers = Collections.unmodifiableList(Arrays.asList(remappers));
    }
}

