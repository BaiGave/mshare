/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class MFDataModel {
    private MFDataModel() {
    }

    @Deprecated
    public static class Markup
    implements Expression {
        public final Kind kind;
        public final String name;
        public final Map<String, Option> options;
        public final List<Attribute> attributes;

        @Deprecated
        public Markup(Kind kind, String name, Map<String, Option> options, List<Attribute> attributes) {
            this.kind = kind;
            this.name = name;
            this.options = options;
            this.attributes = attributes;
        }

        static enum Kind {
            OPEN,
            CLOSE,
            STANDALONE;

        }
    }

    @Deprecated
    public static class Option {
        public final String name;
        public final LiteralOrVariableRef value;

        @Deprecated
        public Option(String name, LiteralOrVariableRef value) {
            this.name = name;
            this.value = value;
        }
    }

    @Deprecated
    public static class VariableRef
    implements LiteralOrVariableRef {
        public final String name;

        @Deprecated
        public VariableRef(String name) {
            this.name = name;
        }
    }

    @Deprecated
    public static class Literal
    implements LiteralOrVariableRef,
    LiteralOrCatchallKey {
        public final String value;

        @Deprecated
        public Literal(String value) {
            this.value = value;
        }
    }

    @Deprecated
    public static interface LiteralOrVariableRef {
    }

    @Deprecated
    public static class Attribute {
        public final String name;
        public final LiteralOrVariableRef value;

        @Deprecated
        public Attribute(String name, LiteralOrVariableRef value) {
            this.name = name;
            this.value = value;
        }
    }

    @Deprecated
    public static class FunctionExpression
    implements Expression {
        public final Function function;
        public final List<Attribute> attributes;

        @Deprecated
        public FunctionExpression(Function function, List<Attribute> attributes) {
            this.function = function;
            this.attributes = attributes;
        }
    }

    @Deprecated
    public static class Function {
        public final String name;
        public final Map<String, Option> options;

        @Deprecated
        public Function(String name, Map<String, Option> options) {
            this.name = name;
            this.options = options;
        }
    }

    @Deprecated
    public static class VariableExpression
    implements Expression {
        public final VariableRef arg;
        public final Function function;
        public final List<Attribute> attributes;

        @Deprecated
        public VariableExpression(VariableRef arg, Function function, List<Attribute> attributes) {
            this.arg = arg;
            this.function = function;
            this.attributes = attributes;
        }
    }

    @Deprecated
    public static class LiteralExpression
    implements Expression {
        public final Literal arg;
        public final Function function;
        public final List<Attribute> attributes;

        @Deprecated
        public LiteralExpression(Literal arg, Function function, List<Attribute> attributes) {
            this.arg = arg;
            this.function = function;
            this.attributes = attributes;
        }
    }

    @Deprecated
    public static interface Expression
    extends PatternPart {
    }

    @Deprecated
    public static class StringPart
    implements PatternPart {
        public final String value;

        StringPart(String value) {
            this.value = value;
        }
    }

    @Deprecated
    public static interface PatternPart {
    }

    @Deprecated
    public static class Pattern {
        public final List<PatternPart> parts = new ArrayList<PatternPart>();

        Pattern() {
        }
    }

    @Deprecated
    public static class CatchallKey
    implements LiteralOrCatchallKey {
        static final String AS_KEY_STRING = "<<::CatchallKey::>>";

        public static boolean isCatchAll(String key) {
            return AS_KEY_STRING.equals(key);
        }
    }

    @Deprecated
    public static class Variant
    implements LiteralOrCatchallKey {
        public final List<LiteralOrCatchallKey> keys;
        public final Pattern value;

        @Deprecated
        public Variant(List<LiteralOrCatchallKey> keys, Pattern value) {
            this.keys = keys;
            this.value = value;
        }
    }

    @Deprecated
    public static interface LiteralOrCatchallKey {
    }

    @Deprecated
    public static class LocalDeclaration
    implements Declaration {
        public final String name;
        public final Expression value;

        @Deprecated
        public LocalDeclaration(String name, Expression value) {
            this.name = name;
            this.value = value;
        }
    }

    @Deprecated
    public static class InputDeclaration
    implements Declaration {
        public final String name;
        public final VariableExpression value;

        @Deprecated
        public InputDeclaration(String name, VariableExpression value) {
            this.name = name;
            this.value = value;
        }
    }

    @Deprecated
    public static interface Declaration {
    }

    @Deprecated
    public static class SelectMessage
    implements Message {
        public final List<Declaration> declarations;
        public final List<Expression> selectors;
        public final List<Variant> variants;

        @Deprecated
        public SelectMessage(List<Declaration> declarations, List<Expression> selectors, List<Variant> variants) {
            this.declarations = declarations;
            this.selectors = selectors;
            this.variants = variants;
        }
    }

    @Deprecated
    public static class PatternMessage
    implements Message {
        public final List<Declaration> declarations;
        public final Pattern pattern;

        @Deprecated
        public PatternMessage(List<Declaration> declarations, Pattern pattern) {
            this.declarations = declarations;
            this.pattern = pattern;
        }
    }

    @Deprecated
    public static interface Message {
    }
}

