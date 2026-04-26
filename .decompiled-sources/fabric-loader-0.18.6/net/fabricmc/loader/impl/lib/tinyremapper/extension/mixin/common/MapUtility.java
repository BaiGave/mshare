/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrLogger;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrRemapper;

public final class MapUtility {
    private final TrRemapper remapper;
    private final TrLogger logger;
    public static final List<String> IGNORED_NAME = Arrays.asList("<init>", "<clinit>");

    public MapUtility(TrRemapper remapper, TrLogger logger) {
        this.remapper = Objects.requireNonNull(remapper);
        this.logger = Objects.requireNonNull(logger);
    }

    public String mapName(TrMember member) {
        if (member.isField()) {
            return this.remapper.mapFieldName(member.getOwner().getName(), member.getName(), member.getDesc());
        }
        return this.remapper.mapMethodName(member.getOwner().getName(), member.getName(), member.getDesc());
    }

    public String mapDesc(TrMember member) {
        if (member.isField()) {
            return this.remapper.mapDesc(member.getDesc());
        }
        return this.remapper.mapMethodDesc(member.getDesc());
    }

    public TrRemapper asTrRemapper() {
        return this.remapper;
    }
}

