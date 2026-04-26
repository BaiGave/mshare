/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

@Deprecated
public class SevenZFileOptions {
    public static final SevenZFileOptions DEFAULT = new SevenZFileOptions(Integer.MAX_VALUE, false, false);
    private final int maxMemoryLimitKiB;
    private final boolean useDefaultNameForUnnamedEntries;
    private final boolean tryToRecoverBrokenArchives;

    public static Builder builder() {
        return new Builder();
    }

    private SevenZFileOptions(int maxMemoryLimitKb, boolean useDefaultNameForUnnamedEntries, boolean tryToRecoverBrokenArchives) {
        this.maxMemoryLimitKiB = maxMemoryLimitKb;
        this.useDefaultNameForUnnamedEntries = useDefaultNameForUnnamedEntries;
        this.tryToRecoverBrokenArchives = tryToRecoverBrokenArchives;
    }

    public int getMaxMemoryLimitInKb() {
        return this.maxMemoryLimitKiB;
    }

    public boolean getTryToRecoverBrokenArchives() {
        return this.tryToRecoverBrokenArchives;
    }

    public boolean getUseDefaultNameForUnnamedEntries() {
        return this.useDefaultNameForUnnamedEntries;
    }

    public static class Builder {
        private int maxMemoryLimitKb = Integer.MAX_VALUE;
        private boolean useDefaultNameForUnnamedEntries = false;
        private boolean tryToRecoverBrokenArchives = false;

        public SevenZFileOptions build() {
            return new SevenZFileOptions(this.maxMemoryLimitKb, this.useDefaultNameForUnnamedEntries, this.tryToRecoverBrokenArchives);
        }

        public Builder withMaxMemoryLimitInKb(int maxMemoryLimitKiB) {
            this.maxMemoryLimitKb = maxMemoryLimitKiB;
            return this;
        }

        public Builder withTryToRecoverBrokenArchives(boolean tryToRecoverBrokenArchives) {
            this.tryToRecoverBrokenArchives = tryToRecoverBrokenArchives;
            return this;
        }

        public Builder withUseDefaultNameForUnnamedEntries(boolean useDefaultNameForUnnamedEntries) {
            this.useDefaultNameForUnnamedEntries = useDefaultNameForUnnamedEntries;
            return this;
        }
    }
}

