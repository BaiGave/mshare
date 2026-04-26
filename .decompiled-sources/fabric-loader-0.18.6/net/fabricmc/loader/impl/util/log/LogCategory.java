/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.log;

public final class LogCategory {
    public static final LogCategory DISCOVERY = LogCategory.create("Discovery");
    public static final LogCategory ENTRYPOINT = LogCategory.create("Entrypoint");
    public static final LogCategory GAME_PATCH = LogCategory.create("GamePatch");
    public static final LogCategory GAME_PROVIDER = LogCategory.create("GameProvider");
    public static final LogCategory GAME_REMAP = LogCategory.create("GameRemap");
    public static final LogCategory GENERAL = LogCategory.create(new String[0]);
    public static final LogCategory KNOT = LogCategory.create("Knot");
    public static final LogCategory LIB_CLASSIFICATION = LogCategory.create("LibClassify");
    public static final LogCategory LOG = LogCategory.create("Log");
    public static final LogCategory MAPPINGS = LogCategory.create("Mappings");
    public static final LogCategory METADATA = LogCategory.create("Metadata");
    public static final LogCategory MOD_REMAP = LogCategory.create("ModRemap");
    public static final LogCategory MIXIN = LogCategory.create("Mixin");
    public static final LogCategory RESOLUTION = LogCategory.create("Resolution");
    public static final LogCategory TEST = LogCategory.create("Test");
    public static final String SEPARATOR = "/";
    public final String context;
    public final String name;
    public Object data;

    public static LogCategory create(String ... names) {
        return new LogCategory("FabricLoader", names);
    }

    public static LogCategory createCustom(String context, String ... names) {
        return new LogCategory(context, names);
    }

    private LogCategory(String context, String[] names) {
        this.context = context;
        this.name = String.join((CharSequence)SEPARATOR, names);
    }

    public String toString() {
        return this.name.isEmpty() ? this.context : this.context + SEPARATOR + this.name;
    }
}

