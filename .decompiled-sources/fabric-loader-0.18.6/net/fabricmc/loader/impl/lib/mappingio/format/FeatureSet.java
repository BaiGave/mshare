/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.mappingio.format;

import net.fabricmc.loader.impl.lib.mappingio.format.FeatureSetUtil;

public interface FeatureSet {
    public boolean hasNamespaces();

    public MetadataSupport fileMetadata();

    public MetadataSupport elementMetadata();

    public NameSupport packages();

    public ClassSupport classes();

    public MemberSupport fields();

    public MemberSupport methods();

    public LocalSupport args();

    public LocalSupport vars();

    public ElementCommentSupport elementComments();

    public boolean hasFileComments();

    default public boolean supportsArgs() {
        return FeatureSetUtil.isSupported(this.args());
    }

    default public boolean supportsVars() {
        return FeatureSetUtil.isSupported(this.vars());
    }

    public static interface NameSupport {
        public FeaturePresence srcNames();

        public FeaturePresence dstNames();
    }

    public static enum FeaturePresence {
        REQUIRED,
        OPTIONAL,
        ABSENT;

    }

    public static interface ClassSupport
    extends NameSupport {
        public boolean hasRepackaging();
    }

    public static interface MemberSupport
    extends DescSupport,
    NameSupport {
    }

    public static interface LocalSupport
    extends DescSupport,
    NameSupport {
        public FeaturePresence positions();

        public FeaturePresence lvIndices();

        public FeaturePresence lvtRowIndices();

        public FeaturePresence startOpIndices();

        public FeaturePresence endOpIndices();
    }

    public static enum ElementCommentSupport {
        NAMESPACED,
        SHARED,
        NONE;

    }

    public static interface DescSupport {
        public FeaturePresence srcDescs();

        public FeaturePresence dstDescs();
    }

    public static enum MetadataSupport {
        NONE,
        FIXED,
        ARBITRARY;

    }
}

