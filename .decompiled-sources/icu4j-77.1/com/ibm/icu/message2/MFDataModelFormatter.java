/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.DateTimeFormatterFactory;
import com.ibm.icu.message2.Directionality;
import com.ibm.icu.message2.FormattedPlaceholder;
import com.ibm.icu.message2.Formatter;
import com.ibm.icu.message2.FormatterFactory;
import com.ibm.icu.message2.IdentityFormatterFactory;
import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.MFFunctionRegistry;
import com.ibm.icu.message2.MessageFormatter;
import com.ibm.icu.message2.NumberFormatterFactory;
import com.ibm.icu.message2.PlainStringFormattedValue;
import com.ibm.icu.message2.Selector;
import com.ibm.icu.message2.SelectorFactory;
import com.ibm.icu.message2.StringUtils;
import com.ibm.icu.message2.TextSelectorFactory;
import com.ibm.icu.util.CurrencyAmount;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class MFDataModelFormatter {
    private static final char LRI = '\u2066';
    private static final char RLI = '\u2067';
    private static final char FSI = '\u2068';
    private static final char PDI = '\u2069';
    private final Locale locale;
    private final MessageFormatter.ErrorHandlingBehavior errorHandlingBehavior;
    private final MessageFormatter.BidiIsolation bidiIsolation;
    private final MFDataModel.Message dm;
    private final MFFunctionRegistry standardFunctions;
    private final MFFunctionRegistry customFunctions;
    private static final MFFunctionRegistry EMPTY_REGISTY = MFFunctionRegistry.builder().build();

    MFDataModelFormatter(MFDataModel.Message dm, Locale locale, MessageFormatter.ErrorHandlingBehavior errorHandlingBehavior, MessageFormatter.BidiIsolation bidiIsolation, MFFunctionRegistry customFunctionRegistry) {
        this.locale = locale;
        this.errorHandlingBehavior = errorHandlingBehavior == null ? MessageFormatter.ErrorHandlingBehavior.BEST_EFFORT : errorHandlingBehavior;
        this.bidiIsolation = bidiIsolation == null ? MessageFormatter.BidiIsolation.NONE : bidiIsolation;
        this.dm = dm;
        this.customFunctions = customFunctionRegistry == null ? EMPTY_REGISTY : customFunctionRegistry;
        this.standardFunctions = MFFunctionRegistry.builder().setFormatter("datetime", new DateTimeFormatterFactory("datetime")).setFormatter("date", new DateTimeFormatterFactory("date")).setFormatter("time", new DateTimeFormatterFactory("time")).setDefaultFormatterNameForType(Date.class, "datetime").setDefaultFormatterNameForType(com.ibm.icu.util.Calendar.class, "datetime").setDefaultFormatterNameForType(Calendar.class, "datetime").setDefaultFormatterNameForType(Temporal.class, "datetime").setFormatter("number", new NumberFormatterFactory("number")).setFormatter("integer", new NumberFormatterFactory("integer")).setFormatter("currency", new NumberFormatterFactory("currency")).setFormatter("math", new NumberFormatterFactory("math")).setDefaultFormatterNameForType(Integer.class, "number").setDefaultFormatterNameForType(Double.class, "number").setDefaultFormatterNameForType(Number.class, "number").setDefaultFormatterNameForType(CurrencyAmount.class, "currency").setFormatter("string", new IdentityFormatterFactory()).setDefaultFormatterNameForType(String.class, "string").setDefaultFormatterNameForType(CharSequence.class, "string").setSelector("number", new NumberFormatterFactory("number")).setSelector("integer", new NumberFormatterFactory("integer")).setSelector("math", new NumberFormatterFactory("math")).setSelector("string", new TextSelectorFactory()).setSelector("icu:gender", new TextSelectorFactory()).build();
    }

    String format(Map<String, Object> arguments) {
        MapWithNfcKeys variables;
        MFDataModel.Pattern patternToRender = null;
        MapWithNfcKeys nfcArguments = new MapWithNfcKeys(arguments);
        if (this.dm instanceof MFDataModel.PatternMessage) {
            MFDataModel.PatternMessage pm = (MFDataModel.PatternMessage)this.dm;
            variables = this.resolveDeclarations(pm.declarations, nfcArguments);
            if (pm.pattern == null) {
                MFDataModelFormatter.fatalFormattingError("The PatternMessage is null.");
            }
            patternToRender = pm.pattern;
        } else if (this.dm instanceof MFDataModel.SelectMessage) {
            MFDataModel.SelectMessage sm = (MFDataModel.SelectMessage)this.dm;
            variables = this.resolveDeclarations(sm.declarations, nfcArguments);
            patternToRender = this.findBestMatchingPattern(sm, variables, nfcArguments);
            if (patternToRender == null) {
                MFDataModelFormatter.fatalFormattingError("Cannor find a match for the selector.");
            }
        } else {
            MFDataModelFormatter.fatalFormattingError("Unknown message type.");
            return "ERROR!";
        }
        Directionality msgdir = Directionality.LTR;
        StringBuilder result = new StringBuilder();
        for (MFDataModel.PatternPart part : patternToRender.parts) {
            if (part instanceof MFDataModel.StringPart) {
                MFDataModel.StringPart strPart = (MFDataModel.StringPart)part;
                result.append(strPart.value);
                continue;
            }
            if (part instanceof MFDataModel.Expression) {
                FormattedPlaceholder formattedExpression = this.formatExpression((MFDataModel.Expression)part, variables, nfcArguments);
                if (this.bidiIsolation == MessageFormatter.BidiIsolation.DEFAULT) {
                    this.implementBiDiDefault(result, msgdir, formattedExpression);
                    continue;
                }
                result.append(formattedExpression.getFormattedValue().toString());
                continue;
            }
            if (part instanceof MFDataModel.Markup) continue;
            MFDataModelFormatter.fatalFormattingError("Unknown part type: " + part);
        }
        return result.toString();
    }

    private void implementBiDiDefault(StringBuilder result, Directionality msgdir, FormattedPlaceholder formattedExpression) {
        String fmt = formattedExpression.getFormattedValue().toString();
        Directionality dir = formattedExpression.getDirectionality();
        boolean isolate = formattedExpression.getIsolate();
        switch (dir) {
            case LTR: {
                if (msgdir == Directionality.LTR && !isolate) {
                    result.append(fmt);
                    break;
                }
                result.append('\u2066').append(fmt).append('\u2069');
                break;
            }
            case RTL: {
                result.append('\u2067').append(fmt).append('\u2069');
                break;
            }
            default: {
                result.append('\u2068').append(fmt).append('\u2069');
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    private MFDataModel.Pattern findBestMatchingPattern(MFDataModel.SelectMessage sm, MapWithNfcKeys variables, MapWithNfcKeys arguments) {
        void var11_37;
        MFDataModel.Pattern patternToRender = null;
        List<MFDataModel.Expression> selectors = sm.selectors;
        ArrayList<ResolvedSelector> res = new ArrayList<ResolvedSelector>(selectors.size());
        for (MFDataModel.Expression sel : selectors) {
            void var10_12;
            FormattedPlaceholder fph = this.formatExpression(sel, variables, arguments);
            Object var10_13 = null;
            Object var11_26 = null;
            MapWithNfcKeys options = new MapWithNfcKeys();
            if (fph.getInput() instanceof ResolvedExpression) {
                ResolvedExpression re = (ResolvedExpression)fph.getInput();
                Object object = re.argument;
                String string = re.functionName;
                options.putAll(re.options);
            } else if (fph.getInput() instanceof MFDataModel.VariableExpression) {
                MFDataModel.VariableExpression ve = (MFDataModel.VariableExpression)fph.getInput();
                Object object = MFDataModelFormatter.resolveLiteralOrVariable(ve.arg, variables, arguments);
                if (ve.function instanceof MFDataModel.Function) {
                    String string = ve.function.name;
                }
            } else if (fph.getInput() instanceof MFDataModel.LiteralExpression) {
                MFDataModel.LiteralExpression le = (MFDataModel.LiteralExpression)fph.getInput();
                MFDataModel.Literal literal = le.arg;
                if (le.function instanceof MFDataModel.Function) {
                    String string = le.function.name;
                }
            }
            SelectorFactory funcFactory = this.standardFunctions.getSelector((String)var10_12);
            if (funcFactory == null) {
                funcFactory = this.customFunctions.getSelector((String)var10_12);
            }
            if (funcFactory != null) {
                void var11_25;
                Selector selectorFunction = funcFactory.createSelector(this.locale, options.getMap());
                ResolvedSelector rs = new ResolvedSelector(var11_25, options, selectorFunction);
                res.add(rs);
                continue;
            }
            MFDataModelFormatter.fatalFormattingError("Unknown selector type: " + (String)var10_12);
        }
        if (res.size() != selectors.size()) {
            MFDataModelFormatter.fatalFormattingError("Something went wrong, not enough selector functions, " + res.size() + " vs. " + selectors.size());
        }
        ArrayList<List<String>> pref = new ArrayList<List<String>>();
        for (int i = 0; i < res.size(); ++i) {
            ArrayList keys = new ArrayList();
            for (MFDataModel.Variant variant : sm.variants) {
                MFDataModel.LiteralOrCatchallKey key = variant.keys.get(i);
                if (key instanceof MFDataModel.CatchallKey) {
                    keys.add("<<::CatchallKey::>>");
                    continue;
                }
                if (key instanceof MFDataModel.Literal) {
                    String ks = ((MFDataModel.Literal)key).value;
                    keys.add(ks);
                    continue;
                }
                MFDataModelFormatter.fatalFormattingError("Literal expected, but got " + key);
            }
            ResolvedSelector resolvedSelector = (ResolvedSelector)res.get(i);
            List<String> list = this.matchSelectorKeys(resolvedSelector, keys);
            pref.add(list);
        }
        ArrayList<MFDataModel.Variant> vars = new ArrayList<MFDataModel.Variant>();
        for (MFDataModel.Variant variant : sm.variants) {
            int n = 0;
            for (int i = 0; i < pref.size(); ++i) {
                MFDataModel.LiteralOrCatchallKey key = variant.keys.get(i);
                if (key instanceof MFDataModel.CatchallKey) {
                    ++n;
                    continue;
                }
                if (!(key instanceof MFDataModel.Literal)) {
                    MFDataModelFormatter.fatalFormattingError("Literal expected");
                }
                String ks = ((MFDataModel.Literal)key).value;
                List matches = (List)pref.get(i);
                if (!matches.contains(ks)) break;
                ++n;
            }
            if (n != pref.size()) continue;
            vars.add(variant);
        }
        ArrayList<IntVarTuple> sortable = new ArrayList<IntVarTuple>();
        for (MFDataModel.Variant variant : vars) {
            IntVarTuple tuple = new IntVarTuple(-1, variant);
            sortable.add(tuple);
        }
        int n = pref.size();
        int n2 = n - 1;
        while (var11_37 >= 0) {
            List matches = (List)pref.get((int)var11_37);
            int minpref = matches.size();
            for (IntVarTuple tuple : sortable) {
                int matchpref = minpref;
                MFDataModel.LiteralOrCatchallKey key = tuple.variant.keys.get((int)var11_37);
                if (!(key instanceof MFDataModel.CatchallKey)) {
                    if (!(key instanceof MFDataModel.Literal)) {
                        MFDataModelFormatter.fatalFormattingError("Literal expected");
                    }
                    String ks = ((MFDataModel.Literal)key).value;
                    matchpref = matches.indexOf(ks);
                }
                tuple.integer = matchpref;
            }
            sortable.sort(MFDataModelFormatter::sortVariants);
            --var11_37;
        }
        IntVarTuple var = (IntVarTuple)sortable.get(0);
        patternToRender = var.variant.value;
        if (patternToRender == null) {
            MFDataModelFormatter.fatalFormattingError("The selection went wrong, cannot select any option.");
        }
        return patternToRender;
    }

    private static int sortVariants(IntVarTuple o1, IntVarTuple o2) {
        int result = Integer.compare(o1.integer, o2.integer);
        if (result != 0) {
            return result;
        }
        List<MFDataModel.LiteralOrCatchallKey> v1 = o1.variant.keys;
        List<MFDataModel.LiteralOrCatchallKey> v2 = o1.variant.keys;
        if (v1.size() != v2.size()) {
            MFDataModelFormatter.fatalFormattingError("The number of keys is not equal.");
        }
        for (int i = 0; i < v1.size(); ++i) {
            MFDataModel.LiteralOrCatchallKey k2;
            String s2;
            MFDataModel.LiteralOrCatchallKey k1 = v1.get(i);
            String s1 = k1 instanceof MFDataModel.Literal ? ((MFDataModel.Literal)k1).value : "<<::CatchallKey::>>";
            int cmp = s1.compareTo(s2 = (k2 = v2.get(i)) instanceof MFDataModel.Literal ? ((MFDataModel.Literal)k2).value : "<<::CatchallKey::>>");
            if (cmp == 0) continue;
            return cmp;
        }
        return 0;
    }

    private List<String> matchSelectorKeys(ResolvedSelector rv, List<String> keys) {
        return rv.selectorFunction.matches(rv.argument, keys, rv.options.getMap());
    }

    private static void fatalFormattingError(String message) throws IllegalArgumentException {
        throw new IllegalArgumentException(message);
    }

    private FormatterFactory getFormattingFunctionFactoryByName(Object toFormat, String functionName) {
        FormatterFactory func;
        if (functionName == null || functionName.isEmpty()) {
            if (toFormat == null) {
                return null;
            }
            Class<?> clazz = toFormat.getClass();
            functionName = this.standardFunctions.getDefaultFormatterNameForType(clazz);
            if (functionName == null) {
                functionName = this.customFunctions.getDefaultFormatterNameForType(clazz);
            }
            if (functionName == null) {
                MFDataModelFormatter.fatalFormattingError("Object to format without a function, and unknown type: " + toFormat.getClass().getName());
            }
        }
        if ((func = this.standardFunctions.getFormatter(functionName)) == null) {
            func = this.customFunctions.getFormatter(functionName);
        }
        return func;
    }

    private static Object resolveLiteralOrVariable(MFDataModel.LiteralOrVariableRef value, MapWithNfcKeys localVars, MapWithNfcKeys arguments) {
        if (value instanceof MFDataModel.Literal) {
            String val = ((MFDataModel.Literal)value).value;
            return val;
        }
        if (value instanceof MFDataModel.VariableRef) {
            String varName = ((MFDataModel.VariableRef)value).name;
            Object val = localVars.get(varName);
            if (val == null) {
                val = localVars.get(varName);
            }
            if (val == null) {
                val = arguments.get(StringUtils.toNfc(varName));
            }
            return val;
        }
        return value;
    }

    private static MapWithNfcKeys convertOptions(Map<String, MFDataModel.Option> options, MapWithNfcKeys localVars, MapWithNfcKeys arguments) {
        MapWithNfcKeys result = new MapWithNfcKeys();
        for (MFDataModel.Option option : options.values()) {
            result.put(option.name, MFDataModelFormatter.resolveLiteralOrVariable(option.value, localVars, arguments));
        }
        return result;
    }

    private FormattedPlaceholder formatExpression(MFDataModel.Expression expression, MapWithNfcKeys variables, MapWithNfcKeys arguments) {
        String res;
        FormatterFactory funcFactory;
        MFDataModel.Function function = null;
        String functionName = null;
        Object toFormat = null;
        HashMap<String, Object> options = new HashMap<String, Object>();
        String fallbackString = "{\ufffd}";
        if (expression instanceof MFDataModel.VariableExpression) {
            MFDataModel.VariableExpression varPart = (MFDataModel.VariableExpression)expression;
            fallbackString = "{$" + varPart.arg.name + "}";
            function = varPart.function;
            Object resolved = MFDataModelFormatter.resolveLiteralOrVariable(varPart.arg, variables, arguments);
            if (resolved instanceof FormattedPlaceholder) {
                Object input = ((FormattedPlaceholder)resolved).getInput();
                if (input instanceof ResolvedExpression) {
                    ResolvedExpression re = (ResolvedExpression)input;
                    toFormat = re.argument;
                    functionName = re.functionName;
                    options.putAll(re.options);
                } else {
                    toFormat = input;
                }
            } else {
                toFormat = resolved;
            }
        } else if (expression instanceof MFDataModel.FunctionExpression) {
            MFDataModel.FunctionExpression fe = (MFDataModel.FunctionExpression)expression;
            fallbackString = "{:" + fe.function.name + "}";
            function = fe.function;
        } else if (expression instanceof MFDataModel.LiteralExpression) {
            MFDataModel.LiteralExpression le = (MFDataModel.LiteralExpression)expression;
            function = le.function;
            fallbackString = "{|" + le.arg.value + "|}";
            toFormat = MFDataModelFormatter.resolveLiteralOrVariable(le.arg, variables, arguments);
        } else {
            if (expression instanceof MFDataModel.Markup) {
                return new FormattedPlaceholder(expression, new PlainStringFormattedValue(""));
            }
            if (expression == null) {
                MFDataModelFormatter.fatalFormattingError("unexpected null expression");
            } else {
                MFDataModelFormatter.fatalFormattingError("unknown expression type " + expression.getClass().getName());
            }
        }
        if (function instanceof MFDataModel.Function) {
            MFDataModel.Function fa = function;
            functionName = fa.name;
            MapWithNfcKeys newOptions = MFDataModelFormatter.convertOptions(fa.options, variables, arguments);
            options.putAll(newOptions.getMap());
        }
        if ((funcFactory = this.getFormattingFunctionFactoryByName(toFormat, functionName)) == null) {
            if (this.errorHandlingBehavior == MessageFormatter.ErrorHandlingBehavior.STRICT) {
                MFDataModelFormatter.fatalFormattingError("unable to find function at " + fallbackString);
            }
            return new FormattedPlaceholder(expression, new PlainStringFormattedValue(fallbackString));
        }
        options.put("icu:impl:errorPolicy", this.errorHandlingBehavior.name());
        Formatter ff = funcFactory.createFormatter(this.locale, options);
        FormattedPlaceholder resultToWrap = ff.format(toFormat, arguments.getMap());
        String string = res = resultToWrap == null ? null : resultToWrap.toString();
        if (res == null) {
            if (this.errorHandlingBehavior == MessageFormatter.ErrorHandlingBehavior.STRICT) {
                MFDataModelFormatter.fatalFormattingError("unable to format string at " + fallbackString);
            }
            res = fallbackString;
        }
        if (resultToWrap != null) {
            toFormat = resultToWrap.getInput();
        }
        ResolvedExpression resExpression = new ResolvedExpression(toFormat, functionName, options);
        if (resultToWrap == null) {
            return new FormattedPlaceholder(resExpression, new PlainStringFormattedValue(res));
        }
        return new FormattedPlaceholder(resExpression, new PlainStringFormattedValue(res), resultToWrap.getDirectionality(), resultToWrap.getIsolate());
    }

    private MapWithNfcKeys resolveDeclarations(List<MFDataModel.Declaration> declarations, MapWithNfcKeys arguments) {
        MapWithNfcKeys variables = new MapWithNfcKeys();
        if (declarations != null) {
            for (MFDataModel.Declaration declaration : declarations) {
                MFDataModel.Expression value;
                String name;
                if (declaration instanceof MFDataModel.InputDeclaration) {
                    name = ((MFDataModel.InputDeclaration)declaration).name;
                    value = ((MFDataModel.InputDeclaration)declaration).value;
                } else {
                    if (!(declaration instanceof MFDataModel.LocalDeclaration)) continue;
                    name = ((MFDataModel.LocalDeclaration)declaration).name;
                    value = ((MFDataModel.LocalDeclaration)declaration).value;
                }
                try {
                    FormattedPlaceholder fmt = this.formatExpression(value, variables, arguments);
                    variables.put(StringUtils.toNfc(name), fmt);
                }
                catch (IllegalArgumentException e) {
                    if (this.errorHandlingBehavior != MessageFormatter.ErrorHandlingBehavior.STRICT) continue;
                    throw e;
                }
                catch (Exception exception) {
                }
            }
        }
        return variables;
    }

    private static class MapWithNfcKeys {
        private final Map<String, Object> theMap = new HashMap<String, Object>();

        Map<String, Object> getMap() {
            return this.theMap;
        }

        MapWithNfcKeys() {
        }

        MapWithNfcKeys(MapWithNfcKeys org) {
            this.theMap.putAll(org.getMap());
        }

        MapWithNfcKeys(Map<String, Object> orgMap) {
            if (orgMap != null) {
                for (Map.Entry<String, Object> e : orgMap.entrySet()) {
                    this.put(StringUtils.toNfc(e.getKey()), e.getValue());
                }
            }
        }

        public Object put(String key, Object value) {
            return this.theMap.put(StringUtils.toNfc(key), value);
        }

        public void putAll(Map<? extends String, ? extends Object> m) {
            this.theMap.putAll(m);
        }

        public Object get(String key) {
            return this.theMap.get(key);
        }
    }

    private static class IntVarTuple {
        int integer;
        final MFDataModel.Variant variant;

        public IntVarTuple(int integer, MFDataModel.Variant variant) {
            this.integer = integer;
            this.variant = variant;
        }
    }

    static class ResolvedExpression
    implements MFDataModel.Expression {
        final Object argument;
        final String functionName;
        final Map<String, Object> options;

        public ResolvedExpression(Object argument, String functionName, Map<String, Object> options) {
            this.argument = argument;
            this.functionName = StringUtils.toNfc(functionName);
            this.options = options;
        }
    }

    private static class ResolvedSelector {
        final Object argument;
        final MapWithNfcKeys options;
        final Selector selectorFunction;

        public ResolvedSelector(Object argument, MapWithNfcKeys options, Selector selectorFunction) {
            this.argument = argument;
            this.options = new MapWithNfcKeys(options);
            this.selectorFunction = selectorFunction;
        }
    }
}

