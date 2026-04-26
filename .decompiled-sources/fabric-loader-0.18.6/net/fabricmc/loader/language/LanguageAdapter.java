/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.language;

import java.io.IOException;
import net.fabricmc.loader.language.JavaLanguageAdapter;
import net.fabricmc.loader.language.LanguageAdapterException;

@Deprecated
public interface LanguageAdapter {
    default public Object createInstance(String classString, Options options) throws ClassNotFoundException, LanguageAdapterException {
        try {
            Class<?> c = JavaLanguageAdapter.getClass(classString, options);
            if (c != null) {
                return this.createInstance(c, options);
            }
            return null;
        }
        catch (IOException e) {
            throw new LanguageAdapterException("I/O error!", e);
        }
    }

    public Object createInstance(Class<?> var1, Options var2) throws LanguageAdapterException;

    public static class Options {
        private MissingSuperclassBehavior missingSuperclassBehavior;

        public MissingSuperclassBehavior getMissingSuperclassBehavior() {
            return this.missingSuperclassBehavior;
        }

        public static class Builder {
            private final Options options = new Options();

            private Builder() {
            }

            public static Builder create() {
                return new Builder();
            }

            public Builder missingSuperclassBehaviour(MissingSuperclassBehavior value) {
                this.options.missingSuperclassBehavior = value;
                return this;
            }

            public Options build() {
                return this.options;
            }
        }
    }

    public static enum MissingSuperclassBehavior {
        RETURN_NULL,
        CRASH;

    }
}

