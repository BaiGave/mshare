/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.FormattedMessage;
import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.MFDataModelFormatter;
import com.ibm.icu.message2.MFFunctionRegistry;
import com.ibm.icu.message2.MFParseException;
import com.ibm.icu.message2.MFParser;
import com.ibm.icu.message2.MFSerializer;
import java.util.Locale;
import java.util.Map;

@Deprecated
public class MessageFormatter {
    private final Locale locale;
    private final String pattern;
    private final ErrorHandlingBehavior errorHandlingBehavior;
    private final BidiIsolation bidiIsolation;
    private final MFFunctionRegistry functionRegistry;
    private final MFDataModel.Message dataModel;
    private final MFDataModelFormatter modelFormatter;

    private MessageFormatter(Builder builder) {
        this.locale = builder.locale;
        this.functionRegistry = builder.functionRegistry;
        this.errorHandlingBehavior = builder.errorHandlingBehavior;
        this.bidiIsolation = builder.bidiIsolation;
        if (builder.pattern == null && builder.dataModel == null || builder.pattern != null && builder.dataModel != null) {
            throw new IllegalArgumentException("You need to set either a pattern, or a dataModel, but not both.");
        }
        if (builder.dataModel != null) {
            this.dataModel = builder.dataModel;
            this.pattern = MFSerializer.dataModelToString(this.dataModel);
        } else {
            this.pattern = builder.pattern;
            try {
                this.dataModel = MFParser.parse(this.pattern);
            }
            catch (MFParseException pe) {
                throw new IllegalArgumentException("Parse error:\nMessage: <<" + this.pattern + ">>\nError: " + pe.getMessage() + "\n");
            }
        }
        this.modelFormatter = new MFDataModelFormatter(this.dataModel, this.locale, this.errorHandlingBehavior, this.bidiIsolation, this.functionRegistry);
    }

    @Deprecated
    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public Locale getLocale() {
        return this.locale;
    }

    @Deprecated
    public ErrorHandlingBehavior getErrorHandlingBehavior() {
        return this.errorHandlingBehavior;
    }

    @Deprecated
    public BidiIsolation getBidiIsolation() {
        return this.bidiIsolation;
    }

    @Deprecated
    public String getPattern() {
        return this.pattern;
    }

    @Deprecated
    public MFDataModel.Message getDataModel() {
        return this.dataModel;
    }

    @Deprecated
    public String formatToString(Map<String, Object> arguments) {
        return this.modelFormatter.format(arguments);
    }

    @Deprecated
    public FormattedMessage format(Map<String, Object> arguments) {
        throw new RuntimeException("Not yet implemented.");
    }

    @Deprecated
    public static class Builder {
        private Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        private String pattern = null;
        private ErrorHandlingBehavior errorHandlingBehavior = ErrorHandlingBehavior.BEST_EFFORT;
        private BidiIsolation bidiIsolation = BidiIsolation.NONE;
        private MFFunctionRegistry functionRegistry = MFFunctionRegistry.builder().build();
        private MFDataModel.Message dataModel = null;

        private Builder() {
        }

        @Deprecated
        public Builder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        @Deprecated
        public Builder setPattern(String pattern) {
            this.pattern = pattern;
            this.dataModel = null;
            return this;
        }

        @Deprecated
        public Builder setErrorHandlingBehavior(ErrorHandlingBehavior errorHandlingBehavior) {
            this.errorHandlingBehavior = errorHandlingBehavior;
            return this;
        }

        @Deprecated
        public Builder setBidiIsolation(BidiIsolation bidiIsolation) {
            this.bidiIsolation = bidiIsolation;
            return this;
        }

        @Deprecated
        public Builder setFunctionRegistry(MFFunctionRegistry functionRegistry) {
            this.functionRegistry = functionRegistry;
            return this;
        }

        @Deprecated
        public Builder setDataModel(MFDataModel.Message dataModel) {
            this.dataModel = dataModel;
            this.pattern = null;
            return this;
        }

        @Deprecated
        public MessageFormatter build() {
            return new MessageFormatter(this);
        }
    }

    @Deprecated
    public static enum BidiIsolation {
        NONE,
        DEFAULT;

    }

    @Deprecated
    public static enum ErrorHandlingBehavior {
        BEST_EFFORT,
        STRICT;

    }
}

