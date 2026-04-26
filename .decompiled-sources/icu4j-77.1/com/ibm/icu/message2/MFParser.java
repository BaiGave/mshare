/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.InputSource;
import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.MFDataModelValidator;
import com.ibm.icu.message2.MFParseException;
import com.ibm.icu.message2.StringUtils;
import com.ibm.icu.message2.StringView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class MFParser {
    private static final int EOF = -1;
    private final InputSource input;
    private static final Pattern RE_NUMBER_LITERAL = Pattern.compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+\\-]?[0-9]+)?");

    MFParser(String text) {
        this.input = new InputSource(text);
    }

    @Deprecated
    public static MFDataModel.Message parse(String input) throws MFParseException {
        return new MFParser(input).parseImpl();
    }

    private MFDataModel.Message parseImpl() throws MFParseException {
        MFDataModel.Message result;
        int savedPosition = this.input.getPosition();
        this.skipOptionalWhitespaces();
        int cp = this.input.peekChar();
        if (cp == 46) {
            result = this.getComplexMessage();
        } else if (cp == 123) {
            cp = this.input.readCodePoint();
            cp = this.input.peekChar();
            if (cp == 123) {
                this.input.backup(1);
                MFDataModel.Pattern pattern = this.getQuotedPattern();
                this.skipOptionalWhitespaces();
                result = new MFDataModel.PatternMessage(new ArrayList<MFDataModel.Declaration>(), pattern);
            } else {
                this.input.gotoPosition(savedPosition);
                MFDataModel.Pattern pattern = this.getPattern();
                result = new MFDataModel.PatternMessage(new ArrayList<MFDataModel.Declaration>(), pattern);
            }
        } else {
            this.input.gotoPosition(savedPosition);
            MFDataModel.Pattern pattern = this.getPattern();
            result = new MFDataModel.PatternMessage(new ArrayList<MFDataModel.Declaration>(), pattern);
        }
        this.checkCondition(this.input.atEnd(), "Content detected after the end of the message.");
        new MFDataModelValidator(result).validate();
        return result;
    }

    private MFDataModel.Pattern getPattern() throws MFParseException {
        MFDataModel.PatternPart part;
        MFDataModel.Pattern pattern = new MFDataModel.Pattern();
        while ((part = this.getPatternPart()) != null) {
            pattern.parts.add(part);
        }
        return pattern;
    }

    private MFDataModel.PatternPart getPatternPart() throws MFParseException {
        int cp = this.input.peekChar();
        switch (cp) {
            case -1: {
                return null;
            }
            case 125: {
                return null;
            }
            case 123: {
                MFDataModel.Expression ph = this.getPlaceholder();
                return ph;
            }
        }
        String plainText = this.getText();
        MFDataModel.StringPart sp = new MFDataModel.StringPart(plainText);
        return sp;
    }

    private String getText() {
        StringBuilder result = new StringBuilder();
        block5: while (true) {
            int cp = this.input.readCodePoint();
            switch (cp) {
                case -1: {
                    return result.toString();
                }
                case 92: {
                    cp = this.input.readCodePoint();
                    if (cp == 92 || cp == 123 || cp == 124 | cp == 125) {
                        result.appendCodePoint(cp);
                        continue block5;
                    }
                    result.appendCodePoint(92);
                    result.appendCodePoint(cp);
                    continue block5;
                }
                case 46: 
                case 64: 
                case 124: {
                    result.appendCodePoint(cp);
                    continue block5;
                }
            }
            if (!StringUtils.isContentChar(cp) && !StringUtils.isWhitespace(cp)) break;
            result.appendCodePoint(cp);
        }
        this.input.backup(1);
        return result.toString();
    }

    private MFDataModel.Expression getPlaceholder() throws MFParseException {
        int cp = this.input.peekChar();
        if (cp != 123) {
            return null;
        }
        this.input.readCodePoint();
        this.skipOptionalWhitespaces();
        cp = this.input.peekChar();
        MFDataModel.Expression result = cp == 35 || cp == 47 ? this.getMarkup() : (cp == 36 ? this.getVariableExpression() : (StringUtils.isFunctionSigil(cp) ? this.getFunctionExpression() : this.getLiteralExpression()));
        this.skipOptionalWhitespaces();
        cp = this.input.readCodePoint();
        this.checkCondition(cp == 125, "Unclosed placeholder");
        return result;
    }

    private MFDataModel.Function getFunction(boolean whitespaceRequired) throws MFParseException {
        int position = this.input.getPosition();
        int cp = this.input.peekChar();
        if (cp == 125) {
            return null;
        }
        int whitespaceCount = 0;
        whitespaceCount = whitespaceRequired ? this.skipRequiredWhitespaces() : this.skipOptionalWhitespaces();
        cp = this.input.peekChar();
        switch (cp) {
            case 125: {
                this.input.backup(whitespaceCount);
                return null;
            }
            case 58: {
                this.input.readCodePoint();
                String identifier = this.getIdentifier();
                this.checkCondition(identifier != null, "Function name missing");
                Map<String, MFDataModel.Option> options = this.getOptions();
                return new MFDataModel.Function(identifier, options);
            }
        }
        this.input.gotoPosition(position);
        return null;
    }

    private MFDataModel.Function getMarkupFunction() throws MFParseException {
        this.skipOptionalWhitespaces();
        int cp = this.input.peekChar();
        switch (cp) {
            case 125: {
                return null;
            }
            case 35: 
            case 47: {
                this.input.readCodePoint();
                String identifier = this.getIdentifier();
                this.checkCondition(identifier != null, "Function name missing");
                Map<String, MFDataModel.Option> options = this.getOptions();
                return new MFDataModel.Function(identifier, options);
            }
        }
        return null;
    }

    private MFDataModel.Expression getLiteralExpression() throws MFParseException {
        MFDataModel.Literal literal = this.getLiteral(false);
        this.checkCondition(literal != null, "Literal expression expected.");
        MFDataModel.Function function = null;
        boolean hasWhitespace = StringUtils.isWhitespace(this.input.peekChar());
        if (!hasWhitespace || (function = this.getFunction(true)) == null) {
            // empty if block
        }
        hasWhitespace = StringUtils.isWhitespace(this.input.peekChar());
        List<MFDataModel.Attribute> attributes = this.getAttributes();
        if (!hasWhitespace && !attributes.isEmpty()) {
            this.error("syntax-error: missing space before attributes");
        }
        return new MFDataModel.LiteralExpression(literal, function, attributes);
    }

    private MFDataModel.VariableExpression getVariableExpression() throws MFParseException {
        MFDataModel.VariableRef variableRef = this.getVariableRef();
        MFDataModel.Function function = this.getFunction(true);
        List<MFDataModel.Attribute> attributes = this.getAttributes();
        return new MFDataModel.VariableExpression(variableRef, function, attributes);
    }

    private MFDataModel.Expression getFunctionExpression() throws MFParseException {
        MFDataModel.Function function = this.getFunction(false);
        List<MFDataModel.Attribute> attributes = this.getAttributes();
        if (function instanceof MFDataModel.Function) {
            return new MFDataModel.FunctionExpression(function, attributes);
        }
        this.error("Unexpected function : " + function);
        return null;
    }

    private MFDataModel.Markup getMarkup() throws MFParseException {
        int cp = this.input.peekChar();
        this.checkCondition(cp == 35 || cp == 47, "Should not happen. Expecting a markup.");
        MFDataModel.Markup.Kind kind = cp == 47 ? MFDataModel.Markup.Kind.CLOSE : MFDataModel.Markup.Kind.OPEN;
        MFDataModel.Function function = this.getMarkupFunction();
        List<MFDataModel.Attribute> attributes = this.getAttributes();
        this.skipOptionalWhitespaces();
        cp = this.input.peekChar();
        if (cp == 47) {
            kind = MFDataModel.Markup.Kind.STANDALONE;
            this.input.readCodePoint();
        }
        if (function instanceof MFDataModel.Function) {
            MFDataModel.Function fa = function;
            return new MFDataModel.Markup(kind, fa.name, fa.options, attributes);
        }
        return null;
    }

    private List<MFDataModel.Attribute> getAttributes() throws MFParseException {
        MFDataModel.Attribute attribute;
        ArrayList<MFDataModel.Attribute> result = new ArrayList<MFDataModel.Attribute>();
        while ((attribute = this.getAttribute()) != null) {
            result.add(attribute);
        }
        return result;
    }

    private MFDataModel.Attribute getAttribute() throws MFParseException {
        int position = this.input.getPosition();
        this.skipOptionalWhitespaces();
        int cp = this.input.peekChar();
        if (cp == 64) {
            this.input.readCodePoint();
            String id = this.getIdentifier();
            int wsCount = this.skipOptionalWhitespaces();
            cp = this.input.peekChar();
            MFDataModel.Literal literalOrVariable = null;
            if (cp == 61) {
                this.input.readCodePoint();
                this.skipOptionalWhitespaces();
                literalOrVariable = this.getLiteral(false);
                this.checkCondition(literalOrVariable != null, "Attributes must have a value after `=`");
            } else {
                this.input.backup(wsCount);
            }
            return new MFDataModel.Attribute(id, literalOrVariable);
        }
        this.input.gotoPosition(position);
        return null;
    }

    private String getIdentifier() throws MFParseException {
        String namespace = this.getName();
        if (namespace == null) {
            return null;
        }
        int cp = this.input.readCodePoint();
        if (cp == 58) {
            String name = this.getName();
            this.checkCondition(name != null, "Expected name after namespace '" + namespace + "'");
            return namespace + ":" + name;
        }
        this.input.backup(1);
        return namespace;
    }

    private Map<String, MFDataModel.Option> getOptions() throws MFParseException {
        MFDataModel.Option option;
        LinkedHashMap<String, MFDataModel.Option> options = new LinkedHashMap<String, MFDataModel.Option>();
        boolean first = true;
        int skipCount = 0;
        while ((option = this.getOption()) != null) {
            this.checkCondition(first || skipCount != 0, "Expected whitespace before option " + option.name);
            first = false;
            if (options.containsKey(option.name)) {
                this.error("Duplicated option '" + option.name + "'");
            }
            options.put(option.name, option);
            skipCount = this.skipOptionalWhitespaces();
        }
        this.input.backup(skipCount);
        return options;
    }

    private MFDataModel.Option getOption() throws MFParseException {
        int position = this.input.getPosition();
        this.skipOptionalWhitespaces();
        String identifier = this.getIdentifier();
        if (identifier == null) {
            this.input.gotoPosition(position);
            return null;
        }
        this.skipOptionalWhitespaces();
        int cp = this.input.readCodePoint();
        this.checkCondition(cp == 61, "Expected '='");
        this.skipOptionalWhitespaces();
        MFDataModel.LiteralOrVariableRef litOrVar = this.getLiteralOrVariableRef();
        if (litOrVar == null) {
            this.error("Options must have a value. An empty string should be quoted.");
        }
        return new MFDataModel.Option(identifier, litOrVar);
    }

    private MFDataModel.LiteralOrVariableRef getLiteralOrVariableRef() throws MFParseException {
        int cp = this.input.peekChar();
        if (cp == 36) {
            return this.getVariableRef();
        }
        return this.getLiteral(false);
    }

    private MFDataModel.Literal getLiteral(boolean normalize) throws MFParseException {
        int cp = this.input.readCodePoint();
        switch (cp) {
            case 124: {
                this.input.backup(1);
                MFDataModel.Literal ql = this.getQuotedLiteral(normalize);
                return ql;
            }
        }
        this.input.backup(1);
        MFDataModel.Literal unql = this.getUnQuotedLiteral(normalize);
        return unql;
    }

    private MFDataModel.VariableRef getVariableRef() throws MFParseException {
        String name;
        int cp = this.input.readCodePoint();
        if (cp != 36) {
            this.checkCondition(cp == 36, "We can't get here");
        }
        this.checkCondition((name = this.getName()) != null, "Invalid variable reference following $");
        return new MFDataModel.VariableRef(name);
    }

    private MFDataModel.Literal getQuotedLiteral(boolean normalize) throws MFParseException {
        StringBuilder result = new StringBuilder();
        int cp = this.input.readCodePoint();
        this.checkCondition(cp == 124, "expected starting '|'");
        while ((cp = this.input.readCodePoint()) != -1) {
            if (StringUtils.isQuotedChar(cp)) {
                result.appendCodePoint(cp);
                continue;
            }
            if (cp != 92) break;
            cp = this.input.readCodePoint();
            boolean isValidEscape = cp == 124 || cp == 92 || cp == 123 || cp == 125;
            this.checkCondition(isValidEscape, "Invalid escape sequence inside quoted literal");
            result.appendCodePoint(cp);
        }
        this.checkCondition(cp == 124, "expected ending '|'");
        return new MFDataModel.Literal(normalize ? StringUtils.toNfc(result) : result.toString());
    }

    private MFDataModel.Literal getUnQuotedLiteral(boolean normalize) throws MFParseException {
        String name = this.getName();
        if (name != null) {
            return new MFDataModel.Literal(normalize ? StringUtils.toNfc(name) : name);
        }
        return this.getNumberLiteral();
    }

    private MFDataModel.Literal getNumberLiteral() {
        String numberString = this.peekWithRegExp(RE_NUMBER_LITERAL);
        if (numberString != null) {
            return new MFDataModel.Literal(numberString);
        }
        return null;
    }

    private int skipRequiredWhitespaces() throws MFParseException {
        int position = this.input.getPosition();
        this.skipOptionalBidi();
        int count = this.skipWhitespaces();
        this.checkCondition(count > 0, "Space expected");
        this.skipOptionalWhitespaces();
        return count;
    }

    private int skipOptionalBidi() {
        int cp;
        int skipCount = 0;
        while (StringUtils.isBidi(cp = this.input.peekChar())) {
            ++skipCount;
            this.input.readCodePoint();
        }
        return skipCount;
    }

    private int skipOptionalWhitespaces() {
        int cp;
        int skipCount = 0;
        while (StringUtils.isWhitespace(cp = this.input.peekChar()) || StringUtils.isBidi(cp)) {
            this.input.readCodePoint();
            ++skipCount;
        }
        return skipCount;
    }

    private int skipWhitespaces() {
        int cp;
        int skipCount = 0;
        while (StringUtils.isWhitespace(cp = this.input.peekChar())) {
            ++skipCount;
            this.input.readCodePoint();
        }
        return skipCount;
    }

    private int skipOneOptionalBidi() {
        int c = this.input.peekChar();
        if (StringUtils.isBidi(c)) {
            this.input.readCodePoint();
            return 1;
        }
        return 0;
    }

    private MFDataModel.Message getComplexMessage() throws MFParseException {
        MFDataModel.Declaration declaration;
        ArrayList<MFDataModel.Declaration> declarations = new ArrayList<MFDataModel.Declaration>();
        boolean foundMatch = false;
        while ((declaration = this.getDeclaration()) != null) {
            if (declaration instanceof MatchDeclaration) {
                foundMatch = true;
                break;
            }
            declarations.add(declaration);
        }
        if (foundMatch) {
            return this.getMatch(declarations);
        }
        this.skipOptionalWhitespaces();
        int cp = this.input.peekChar();
        this.checkCondition(cp != -1, "Expected a quoted pattern or .match; got end-of-input");
        MFDataModel.Pattern pattern = this.getQuotedPattern();
        this.skipOptionalWhitespaces();
        this.checkCondition(this.input.atEnd(), "Content detected after the end of the message.");
        return new MFDataModel.PatternMessage(declarations, pattern);
    }

    private MFDataModel.SelectMessage getMatch(List<MFDataModel.Declaration> declarations) throws MFParseException {
        MFDataModel.Variant variant;
        ArrayList<MFDataModel.Expression> expressions = new ArrayList<MFDataModel.Expression>();
        while (true) {
            MFDataModel.VariableRef variableRef;
            this.skipRequiredWhitespaces();
            int cp = this.input.peekChar();
            if (cp != 36 || (variableRef = this.getVariableRef()) == null) break;
            MFDataModel.VariableExpression expression = new MFDataModel.VariableExpression(variableRef, null, new ArrayList<MFDataModel.Attribute>());
            expressions.add(expression);
        }
        this.checkCondition(!expressions.isEmpty(), "There should be at least one selector expression.");
        ArrayList<MFDataModel.Variant> variants = new ArrayList<MFDataModel.Variant>();
        while ((variant = this.getVariant()) != null) {
            variants.add(variant);
        }
        this.checkCondition(this.input.atEnd(), "Content detected after the end of the message.");
        return new MFDataModel.SelectMessage(declarations, expressions, variants);
    }

    private MFDataModel.Variant getVariant() throws MFParseException {
        MFDataModel.LiteralOrCatchallKey key;
        ArrayList<MFDataModel.LiteralOrCatchallKey> keys = new ArrayList<MFDataModel.LiteralOrCatchallKey>();
        while ((key = this.getKey(!keys.isEmpty())) != null) {
            keys.add(key);
        }
        this.skipOptionalWhitespaces();
        if (this.input.atEnd()) {
            this.checkCondition(keys.isEmpty(), "After selector keys it is mandatory to have a pattern.");
            return null;
        }
        MFDataModel.Pattern pattern = this.getQuotedPattern();
        return new MFDataModel.Variant(keys, pattern);
    }

    private MFDataModel.LiteralOrCatchallKey getKey(boolean requireSpaces) throws MFParseException {
        int cp = this.input.peekChar();
        if (cp == 123) {
            return null;
        }
        int skipCount = 0;
        skipCount = requireSpaces ? this.skipRequiredWhitespaces() : this.skipOptionalWhitespaces();
        cp = this.input.peekChar();
        if (cp == 42) {
            this.input.readCodePoint();
            return new MFDataModel.CatchallKey();
        }
        if (cp == -1) {
            this.input.backup(skipCount);
            return null;
        }
        return this.getLiteral(true);
    }

    private MFDataModel.Declaration getDeclaration() throws MFParseException {
        int position = this.input.getPosition();
        this.skipOptionalWhitespaces();
        int cp = this.input.readCodePoint();
        if (cp != 46) {
            this.input.gotoPosition(position);
            return null;
        }
        String declName = this.getName();
        this.checkCondition(declName != null, "Expected a declaration after the '.'");
        switch (declName) {
            case "input": {
                this.skipOptionalWhitespaces();
                MFDataModel.Expression expression = this.getPlaceholder();
                String inputVarName = null;
                this.checkCondition(expression instanceof MFDataModel.VariableExpression, "Variable expression required in .input declaration");
                inputVarName = ((MFDataModel.VariableExpression)expression).arg.name;
                return new MFDataModel.InputDeclaration(inputVarName, (MFDataModel.VariableExpression)expression);
            }
            case "local": {
                this.skipRequiredWhitespaces();
                MFDataModel.VariableRef varName = this.getVariableRef();
                this.skipOptionalWhitespaces();
                cp = this.input.readCodePoint();
                this.checkCondition(cp == 61, declName);
                this.skipOptionalWhitespaces();
                MFDataModel.Expression expression = this.getPlaceholder();
                if (!(varName instanceof MFDataModel.VariableRef)) break;
                return new MFDataModel.LocalDeclaration(varName.name, expression);
            }
            case "match": {
                return new MatchDeclaration();
            }
        }
        return null;
    }

    private MFDataModel.Pattern getQuotedPattern() throws MFParseException {
        int cp = this.input.readCodePoint();
        this.checkCondition(cp == 123, "Expected { for a complex body");
        cp = this.input.readCodePoint();
        this.checkCondition(cp == 123, "Expected second { for a complex body");
        MFDataModel.Pattern pattern = this.getPattern();
        cp = this.input.readCodePoint();
        this.checkCondition(cp == 125, "Expected } to end a complex body");
        cp = this.input.readCodePoint();
        this.checkCondition(cp == 125, "Expected second } to end a complex body");
        return pattern;
    }

    private String getName() throws MFParseException {
        int savedPosition = this.input.getPosition();
        StringBuilder result = new StringBuilder();
        this.skipOneOptionalBidi();
        int cp = this.input.readCodePoint();
        this.checkCondition(cp != -1, "Expected name or namespace.");
        if (!StringUtils.isNameStart(cp)) {
            this.input.gotoPosition(savedPosition);
            return null;
        }
        result.appendCodePoint(cp);
        while (StringUtils.isNameChar(cp = this.input.readCodePoint())) {
            result.appendCodePoint(cp);
        }
        if (cp != -1) {
            this.input.backup(1);
        }
        this.skipOneOptionalBidi();
        return StringUtils.toNfc(result.toString());
    }

    private void checkCondition(boolean condition, String message) throws MFParseException {
        if (!condition) {
            this.error(message);
        }
    }

    private void error(String message) throws MFParseException {
        StringBuilder finalMsg = new StringBuilder();
        if (this.input == null) {
            finalMsg.append("Parse error: ");
            finalMsg.append(message);
        } else {
            int position = this.input.getPosition();
            finalMsg.append("Parse error [" + this.input.getPosition() + "]: ");
            finalMsg.append(message);
            finalMsg.append("\n");
            if (position != -1) {
                finalMsg.append(this.input.buffer.substring(0, position));
                finalMsg.append("^^^");
                finalMsg.append(this.input.buffer.substring(position));
            } else {
                finalMsg.append(this.input.buffer);
                finalMsg.append("^^^");
            }
        }
        throw new MFParseException(finalMsg.toString(), this.input.getPosition());
    }

    private String peekWithRegExp(Pattern pattern) {
        StringView sv = new StringView(this.input.buffer, this.input.getPosition());
        Matcher m = pattern.matcher(sv);
        if (m.find()) {
            this.input.skip(m.group().length());
            return m.group();
        }
        return null;
    }

    private static class MatchDeclaration
    implements MFDataModel.Declaration {
        private MatchDeclaration() {
        }
    }
}

