/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class MFSerializer {
    private boolean shouldDoubleQuotePattern = false;
    private boolean needSpace = false;
    private final StringBuilder result = new StringBuilder();
    private static final Pattern RE_NUMBER_LITERAL = Pattern.compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+\\-]?[0-9]+)?$");

    @Deprecated
    public static String dataModelToString(MFDataModel.Message message) {
        return new MFSerializer().messageToString(message);
    }

    private String messageToString(MFDataModel.Message message) {
        if (message instanceof MFDataModel.PatternMessage) {
            this.patternMessageToString((MFDataModel.PatternMessage)message);
        } else if (message instanceof MFDataModel.SelectMessage) {
            this.selectMessageToString((MFDataModel.SelectMessage)message);
        } else {
            this.errorType("Message", message);
        }
        return this.result.toString();
    }

    private void selectMessageToString(MFDataModel.SelectMessage message) {
        this.declarationsToString(message.declarations);
        this.shouldDoubleQuotePattern = true;
        this.addSpaceIfNeeded();
        this.result.append(".match");
        for (MFDataModel.Expression selector : message.selectors) {
            this.result.append(' ');
            if (selector instanceof MFDataModel.VariableExpression) {
                MFDataModel.VariableExpression ve = (MFDataModel.VariableExpression)selector;
                this.literalOrVariableRefToString(ve.arg);
                continue;
            }
            this.expressionToString(selector);
        }
        for (MFDataModel.Variant variant : message.variants) {
            this.variantToString(variant);
        }
    }

    private void patternMessageToString(MFDataModel.PatternMessage message) {
        this.declarationsToString(message.declarations);
        this.patternToString(message.pattern);
    }

    private void patternToString(MFDataModel.Pattern pattern) {
        this.addSpaceIfNeeded();
        if (this.shouldDoubleQuotePattern) {
            this.result.append("{{");
        }
        for (MFDataModel.PatternPart part : pattern.parts) {
            if (part instanceof MFDataModel.StringPart) {
                this.stringPartToString((MFDataModel.StringPart)part);
                continue;
            }
            this.expressionToString((MFDataModel.Expression)part);
        }
        if (this.shouldDoubleQuotePattern) {
            this.result.append("}}");
        }
    }

    private void expressionToString(MFDataModel.Expression expression) {
        if (expression == null) {
            return;
        }
        if (expression instanceof MFDataModel.LiteralExpression) {
            this.literalExpressionToString((MFDataModel.LiteralExpression)expression);
        } else if (expression instanceof MFDataModel.VariableExpression) {
            this.variableExpressionToString((MFDataModel.VariableExpression)expression);
        } else if (expression instanceof MFDataModel.FunctionExpression) {
            this.functionExpressionToString((MFDataModel.FunctionExpression)expression);
        } else if (expression instanceof MFDataModel.Markup) {
            this.markupToString((MFDataModel.Markup)expression);
        } else {
            this.errorType("Expression", expression);
        }
    }

    private void markupToString(MFDataModel.Markup markup) {
        this.result.append('{');
        if (markup.kind == MFDataModel.Markup.Kind.CLOSE) {
            this.result.append('/');
        } else {
            this.result.append('#');
        }
        this.result.append(markup.name);
        this.optionsToString(markup.options);
        this.attributesToString(markup.attributes);
        if (markup.kind == MFDataModel.Markup.Kind.STANDALONE) {
            this.result.append('/');
        }
        this.result.append('}');
    }

    private void optionsToString(Map<String, MFDataModel.Option> options) {
        for (MFDataModel.Option option : options.values()) {
            this.result.append(' ');
            this.result.append(option.name);
            this.result.append('=');
            this.literalOrVariableRefToString(option.value);
        }
    }

    private void functionExpressionToString(MFDataModel.FunctionExpression fe) {
        this.result.append('{');
        this.functionToString(fe.function);
        this.attributesToString(fe.attributes);
        this.result.append('}');
    }

    private void attributesToString(List<MFDataModel.Attribute> attributes) {
        if (attributes == null) {
            return;
        }
        for (MFDataModel.Attribute attribute : attributes) {
            this.result.append(" @");
            this.result.append(attribute.name);
            if (attribute.value == null) continue;
            this.result.append('=');
            this.literalOrVariableRefToString(attribute.value);
        }
    }

    private void functionToString(MFDataModel.Function function) {
        if (function == null) {
            return;
        }
        if (function instanceof MFDataModel.Function) {
            this.addSpaceIfNeeded();
            this.result.append(":");
            this.result.append(function.name);
            this.optionsToString(function.options);
        } else {
            this.errorType("Function", function);
        }
    }

    private void variableExpressionToString(MFDataModel.VariableExpression ve) {
        if (ve == null) {
            return;
        }
        this.result.append('{');
        this.literalOrVariableRefToString(ve.arg);
        this.needSpace = true;
        this.functionToString(ve.function);
        this.attributesToString(ve.attributes);
        this.result.append('}');
        this.needSpace = false;
    }

    private void literalOrVariableRefToString(MFDataModel.LiteralOrVariableRef literalOrVarRef) {
        if (literalOrVarRef instanceof MFDataModel.Literal) {
            this.literalToString((MFDataModel.Literal)literalOrVarRef);
        } else if (literalOrVarRef instanceof MFDataModel.VariableRef) {
            this.result.append("$" + ((MFDataModel.VariableRef)literalOrVarRef).name);
        } else {
            this.errorType("LiteralOrVariableRef", literalOrVarRef);
        }
    }

    private void literalToString(MFDataModel.Literal literal) {
        String value = literal.value;
        Matcher matcher = RE_NUMBER_LITERAL.matcher(value);
        if (matcher.find()) {
            this.result.append(value);
        } else {
            StringBuilder literalBuffer = new StringBuilder();
            boolean wasName = true;
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                if (c == '\\' || c == '|') {
                    literalBuffer.append('\\');
                }
                literalBuffer.append(c);
                if (i == 0 && !StringUtils.isNameStart(c)) {
                    wasName = false;
                    continue;
                }
                if (StringUtils.isNameChar(c)) continue;
                wasName = false;
            }
            if (wasName && literalBuffer.length() != 0) {
                this.result.append((CharSequence)literalBuffer);
            } else {
                this.result.append('|');
                this.result.append((CharSequence)literalBuffer);
                this.result.append('|');
            }
        }
    }

    private void literalExpressionToString(MFDataModel.LiteralExpression le) {
        this.result.append('{');
        this.literalOrVariableRefToString(le.arg);
        this.needSpace = true;
        this.functionToString(le.function);
        this.attributesToString(le.attributes);
        this.result.append('}');
    }

    private void stringPartToString(MFDataModel.StringPart part) {
        if (part.value.startsWith(".") && !this.shouldDoubleQuotePattern) {
            this.shouldDoubleQuotePattern = true;
            this.result.append("{{");
        }
        for (int i = 0; i < part.value.length(); ++i) {
            char c = part.value.charAt(i);
            if (c == '\\' || c == '{' || c == '}') {
                this.result.append('\\');
            }
            this.result.append(c);
        }
    }

    private void declarationsToString(List<MFDataModel.Declaration> declarations) {
        if (declarations == null || declarations.isEmpty()) {
            return;
        }
        this.shouldDoubleQuotePattern = true;
        for (MFDataModel.Declaration declaration : declarations) {
            if (declaration instanceof MFDataModel.LocalDeclaration) {
                this.localDeclarationToString((MFDataModel.LocalDeclaration)declaration);
                continue;
            }
            if (declaration instanceof MFDataModel.InputDeclaration) {
                this.inputDeclarationToString((MFDataModel.InputDeclaration)declaration);
                continue;
            }
            this.errorType("Declaration", declaration);
        }
    }

    private void inputDeclarationToString(MFDataModel.InputDeclaration declaration) {
        this.addSpaceIfNeeded();
        this.result.append(".input ");
        this.variableExpressionToString(declaration.value);
        this.needSpace = true;
    }

    private void localDeclarationToString(MFDataModel.LocalDeclaration declaration) {
        this.addSpaceIfNeeded();
        this.result.append(".local $");
        this.result.append(declaration.name);
        this.result.append(" = ");
        this.expressionToString(declaration.value);
        this.needSpace = true;
    }

    private void variantToString(MFDataModel.Variant variant) {
        for (MFDataModel.LiteralOrCatchallKey key : variant.keys) {
            this.result.append(' ');
            if (key instanceof MFDataModel.CatchallKey) {
                this.result.append('*');
                continue;
            }
            this.literalToString((MFDataModel.Literal)key);
        }
        this.result.append(' ');
        this.patternToString(variant.value);
    }

    private void addSpaceIfNeeded() {
        if (this.needSpace) {
            this.result.append(' ');
            this.needSpace = false;
        }
    }

    private void errorType(String expectedType, Object obj) {
        this.error("Unexpected '" + expectedType + "' type: ", obj);
    }

    private void error(String text, Object obj) {
        this.error(text + obj.getClass().getName());
    }

    private void error(String text) {
        throw new RuntimeException(text);
    }
}

