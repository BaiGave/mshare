/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public interface ModDependency {
    public Kind getKind();

    public String getModId();

    public boolean matches(Version var1);

    public Collection<VersionPredicate> getVersionRequirements();

    public List<VersionInterval> getVersionIntervals();

    public static enum Kind {
        DEPENDS("depends", true, false),
        RECOMMENDS("recommends", true, true),
        SUGGESTS("suggests", true, true),
        CONFLICTS("conflicts", false, true),
        BREAKS("breaks", false, false);

        private static final Map<String, Kind> map;
        private final String key;
        private final boolean positive;
        private final boolean soft;

        private Kind(String key, boolean positive, boolean soft) {
            this.key = key;
            this.positive = positive;
            this.soft = soft;
        }

        public String getKey() {
            return this.key;
        }

        public boolean isPositive() {
            return this.positive;
        }

        public boolean isSoft() {
            return this.soft;
        }

        public static Kind parse(String key) {
            return map.get(key);
        }

        private static Map<String, Kind> createMap() {
            Kind[] values = Kind.values();
            HashMap<String, Kind> ret = new HashMap<String, Kind>(values.length);
            for (Kind kind : values) {
                ret.put(kind.key, kind);
            }
            return ret;
        }

        static {
            map = Kind.createMap();
        }
    }
}

