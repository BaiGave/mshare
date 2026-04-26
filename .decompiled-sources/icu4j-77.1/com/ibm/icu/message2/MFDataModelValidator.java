/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.MFParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

class MFDataModelValidator {
    private final MFDataModel.Message message;
    private final Set<String> declaredVars = new HashSet<String>();

    MFDataModelValidator(MFDataModel.Message message) {
        this.message = message;
    }

    boolean validate() throws MFParseException {
        if (this.message instanceof MFDataModel.PatternMessage) {
            this.validateDeclarations(((MFDataModel.PatternMessage)this.message).declarations);
        } else if (this.message instanceof MFDataModel.SelectMessage) {
            MFDataModel.SelectMessage sm = (MFDataModel.SelectMessage)this.message;
            this.validateDeclarations(sm.declarations);
            this.validateSelectors(sm.selectors);
            int selectorCount = sm.selectors.size();
            this.validateVariants(sm.variants, selectorCount);
        }
        return true;
    }

    private boolean validateVariants(List<MFDataModel.Variant> variants, int selectorCount) throws MFParseException {
        if (variants == null || variants.isEmpty()) {
            this.error("Selection messages must have at least one variant");
        }
        boolean hasUltimateFallback = false;
        HashSet<String> fakeKeys = new HashSet<String>();
        for (MFDataModel.Variant variant : variants) {
            if (variant.keys == null || variant.keys.isEmpty()) {
                this.error("Selection variants must have at least one key");
            }
            if (variant.keys.size() != selectorCount) {
                this.error("Selection variants must have the same number of variants as the selectors.");
            }
            int catchAllCount = 0;
            StringJoiner fakeKey = new StringJoiner("<<::>>");
            for (MFDataModel.LiteralOrCatchallKey key : variant.keys) {
                if (key instanceof MFDataModel.CatchallKey) {
                    ++catchAllCount;
                    fakeKey.add("<<::CatchallKey::>>");
                    continue;
                }
                if (!(key instanceof MFDataModel.Literal)) continue;
                fakeKey.add(((MFDataModel.Literal)key).value);
            }
            if (fakeKeys.contains(fakeKey.toString())) {
                this.error("Dumplicate combination of keys");
            } else {
                fakeKeys.add(fakeKey.toString());
            }
            if (catchAllCount != selectorCount) continue;
            hasUltimateFallback = true;
        }
        if (!hasUltimateFallback) {
            this.error("There must be one variant with all the keys being '*'");
        }
        return true;
    }

    private boolean validateSelectors(List<MFDataModel.Expression> selectors) throws MFParseException {
        if (selectors == null || selectors.isEmpty()) {
            this.error("Selection messages must have selectors");
        }
        return true;
    }

    private boolean validateDeclarations(List<MFDataModel.Declaration> declarations) throws MFParseException {
        if (declarations == null || declarations.isEmpty()) {
            return true;
        }
        for (MFDataModel.Declaration declaration : declarations) {
            if (declaration instanceof MFDataModel.LocalDeclaration) {
                MFDataModel.LocalDeclaration ld = (MFDataModel.LocalDeclaration)declaration;
                this.validateExpression(ld.value, false);
                this.addVariableDeclaration(ld.name);
                continue;
            }
            if (!(declaration instanceof MFDataModel.InputDeclaration)) continue;
            MFDataModel.InputDeclaration id = (MFDataModel.InputDeclaration)declaration;
            this.validateExpression(id.value, true);
        }
        return true;
    }

    private void validateExpression(MFDataModel.Expression expression, boolean fromInput) throws MFParseException {
        String argName = null;
        boolean wasLiteral = false;
        MFDataModel.Function function = null;
        if (!(expression instanceof MFDataModel.Literal)) {
            if (expression instanceof MFDataModel.LiteralExpression) {
                MFDataModel.LiteralExpression le = (MFDataModel.LiteralExpression)expression;
                argName = le.arg.value;
                function = le.function;
                wasLiteral = true;
            } else if (expression instanceof MFDataModel.VariableExpression) {
                MFDataModel.VariableExpression ve = (MFDataModel.VariableExpression)expression;
                argName = ve.arg.name;
                function = ve.function;
            } else if (expression instanceof MFDataModel.FunctionExpression) {
                MFDataModel.FunctionExpression fe = (MFDataModel.FunctionExpression)expression;
                function = fe.function;
            }
        }
        if (function instanceof MFDataModel.Function) {
            MFDataModel.Function fa = function;
            if (fa.options != null) {
                for (MFDataModel.Option opt : fa.options.values()) {
                    MFDataModel.LiteralOrVariableRef val = opt.value;
                    if (!(val instanceof MFDataModel.VariableRef)) continue;
                    this.addVariableDeclaration(((MFDataModel.VariableRef)val).name);
                }
            }
        }
        if (argName != null) {
            if (fromInput) {
                this.addVariableDeclaration(argName);
            } else if (!wasLiteral) {
                this.declaredVars.add(argName);
            }
        }
    }

    private boolean addVariableDeclaration(String varName) throws MFParseException {
        if (this.declaredVars.contains(varName)) {
            this.error("Variable '" + varName + "' already declared");
            return false;
        }
        this.declaredVars.add(varName);
        return true;
    }

    private void error(String text) throws MFParseException {
        throw new MFParseException(text, -1);
    }
}

