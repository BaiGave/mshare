/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.personname;

import com.ibm.icu.impl.personname.FieldModifierImpl;
import com.ibm.icu.impl.personname.PersonNameFormatterImpl;
import com.ibm.icu.text.PersonName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

class PersonNamePattern {
    private String patternText;
    private Element[] patternElements;

    public static PersonNamePattern[] makePatterns(String[] patternText, PersonNameFormatterImpl formatterImpl) {
        PersonNamePattern[] result = new PersonNamePattern[patternText.length];
        for (int i = 0; i < patternText.length; ++i) {
            result[i] = new PersonNamePattern(patternText[i], formatterImpl);
        }
        return result;
    }

    public String toString() {
        return this.patternText;
    }

    private PersonNamePattern(String patternText, PersonNameFormatterImpl formatterImpl) {
        this.patternText = patternText;
        ArrayList<Element> elements = new ArrayList<Element>();
        boolean inField = false;
        boolean inEscape = false;
        StringBuilder workingString = new StringBuilder();
        block5: for (int i = 0; i < patternText.length(); ++i) {
            char c = patternText.charAt(i);
            if (inEscape) {
                workingString.append(c);
                inEscape = false;
                continue;
            }
            switch (c) {
                case '\\': {
                    inEscape = true;
                    continue block5;
                }
                case '{': {
                    if (!inField) {
                        if (workingString.length() > 0) {
                            elements.add(new LiteralText(workingString.toString()));
                            workingString = new StringBuilder();
                        }
                        inField = true;
                        continue block5;
                    }
                    throw new IllegalArgumentException("Nested braces are not allowed in name patterns");
                }
                case '}': {
                    if (inField) {
                        if (workingString.length() <= 0) {
                            throw new IllegalArgumentException("No field name inside braces");
                        }
                        elements.add(new NameFieldImpl(workingString.toString(), formatterImpl));
                        workingString = new StringBuilder();
                        inField = false;
                        continue block5;
                    }
                    throw new IllegalArgumentException("Unmatched closing brace in literal text");
                }
                default: {
                    workingString.append(c);
                }
            }
        }
        if (workingString.length() > 0) {
            elements.add(new LiteralText(workingString.toString()));
        }
        this.patternElements = elements.toArray(new Element[0]);
    }

    public String format(PersonName name) {
        StringBuilder result = new StringBuilder();
        boolean seenLeadingField = false;
        boolean seenEmptyLeadingField = false;
        boolean seenEmptyField = false;
        StringBuilder textBefore = new StringBuilder();
        StringBuilder textAfter = new StringBuilder();
        name = this.hackNameForEmptyFields(name);
        for (Element element : this.patternElements) {
            if (element.isLiteral()) {
                if (seenEmptyLeadingField) continue;
                if (seenEmptyField) {
                    textAfter.append(element.format(name));
                    continue;
                }
                textBefore.append(element.format(name));
                continue;
            }
            String fieldText = element.format(name);
            if (fieldText == null || fieldText.isEmpty()) {
                if (!seenLeadingField) {
                    seenEmptyLeadingField = true;
                    textBefore.setLength(0);
                    continue;
                }
                seenEmptyField = true;
                textAfter.setLength(0);
                continue;
            }
            seenLeadingField = true;
            seenEmptyLeadingField = false;
            if (seenEmptyField) {
                result.append(this.coalesce(textBefore, textAfter));
                result.append(fieldText);
                seenEmptyField = false;
                continue;
            }
            result.append((CharSequence)textBefore);
            textBefore.setLength(0);
            result.append(element.format(name));
        }
        if (!seenEmptyField) {
            result.append((CharSequence)textBefore);
        }
        return result.toString();
    }

    public int numPopulatedFields(PersonName name) {
        int result = 0;
        for (Element element : this.patternElements) {
            result += element.isPopulated(name) ? 1 : 0;
        }
        return result;
    }

    public int numEmptyFields(PersonName name) {
        int result = 0;
        for (Element element : this.patternElements) {
            result += !element.isLiteral() && !element.isPopulated(name) ? 1 : 0;
        }
        return result;
    }

    private String coalesce(StringBuilder s1, StringBuilder s2) {
        int p2;
        int p1;
        if (this.endsWith(s1, s2)) {
            s2.setLength(0);
        }
        for (p1 = 0; p1 < s1.length() && !Character.isWhitespace(s1.charAt(p1)); ++p1) {
        }
        for (p2 = s2.length() - 1; p2 >= 0 && !Character.isWhitespace(s2.charAt(p2)); --p2) {
        }
        if (p1 < s1.length()) {
            ++p1;
        } else if (p2 >= 0) {
            --p2;
        }
        String result = s1.substring(0, p1) + s2.substring(p2 + 1);
        s1.setLength(0);
        s2.setLength(0);
        return result;
    }

    private boolean endsWith(StringBuilder s1, StringBuilder s2) {
        int p2;
        int p1 = s1.length() - 1;
        for (p2 = s2.length() - 1; p1 >= 0 && p2 >= 0 && s1.charAt(p1) == s2.charAt(p2); --p1, --p2) {
        }
        return p2 < 0;
    }

    private PersonName hackNameForEmptyFields(PersonName originalName) {
        PersonName result = originalName;
        if (originalName.getFieldValue(PersonName.NameField.SURNAME, Collections.emptySet()) == null) {
            boolean patternHasNonInitialGivenName = false;
            for (Element element : this.patternElements) {
                if (element.isLiteral() || ((NameFieldImpl)element).fieldID != PersonName.NameField.GIVEN || ((NameFieldImpl)element).modifiers.containsKey((Object)PersonName.FieldModifier.INITIAL)) continue;
                patternHasNonInitialGivenName = true;
                break;
            }
            if (!patternHasNonInitialGivenName) {
                return new GivenToSurnamePersonName(originalName);
            }
        }
        return result;
    }

    private static class GivenToSurnamePersonName
    implements PersonName {
        private PersonName underlyingPersonName;

        public GivenToSurnamePersonName(PersonName underlyingPersonName) {
            this.underlyingPersonName = underlyingPersonName;
        }

        public String toString() {
            return "Inverted version of " + this.underlyingPersonName.toString();
        }

        @Override
        public Locale getNameLocale() {
            return this.underlyingPersonName.getNameLocale();
        }

        @Override
        public PersonName.PreferredOrder getPreferredOrder() {
            return this.underlyingPersonName.getPreferredOrder();
        }

        @Override
        public String getFieldValue(PersonName.NameField identifier, Set<PersonName.FieldModifier> modifiers) {
            if (identifier == PersonName.NameField.SURNAME) {
                return this.underlyingPersonName.getFieldValue(PersonName.NameField.GIVEN, modifiers);
            }
            if (identifier == PersonName.NameField.GIVEN) {
                return null;
            }
            return this.underlyingPersonName.getFieldValue(identifier, modifiers);
        }
    }

    private static class NameFieldImpl
    implements Element {
        private PersonName.NameField fieldID;
        private Map<PersonName.FieldModifier, FieldModifierImpl> modifiers;

        public NameFieldImpl(String fieldNameAndModifiers, PersonNameFormatterImpl formatterImpl) {
            ArrayList<PersonName.FieldModifier> modifierIDs = new ArrayList<PersonName.FieldModifier>();
            StringTokenizer tok = new StringTokenizer(fieldNameAndModifiers, "-");
            this.fieldID = PersonName.NameField.forString(tok.nextToken());
            while (tok.hasMoreTokens()) {
                modifierIDs.add(PersonName.FieldModifier.forString(tok.nextToken()));
            }
            if (this.fieldID == PersonName.NameField.SURNAME && formatterImpl.shouldCapitalizeSurname()) {
                modifierIDs.add(PersonName.FieldModifier.ALL_CAPS);
            }
            this.modifiers = new HashMap<PersonName.FieldModifier, FieldModifierImpl>();
            for (PersonName.FieldModifier modifierID : modifierIDs) {
                this.modifiers.put(modifierID, FieldModifierImpl.forName(modifierID, formatterImpl));
            }
            if (this.modifiers.containsKey((Object)PersonName.FieldModifier.RETAIN) && this.modifiers.containsKey((Object)PersonName.FieldModifier.INITIAL)) {
                FieldModifierImpl.InitialModifier initialModifier = (FieldModifierImpl.InitialModifier)this.modifiers.get((Object)PersonName.FieldModifier.INITIAL);
                initialModifier.setRetainPunctuation(true);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append((Object)this.fieldID);
            for (PersonName.FieldModifier modifier : this.modifiers.keySet()) {
                sb.append("-");
                sb.append(modifier.toString());
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public String format(PersonName name) {
            HashSet<PersonName.FieldModifier> modifierIDs = new HashSet<PersonName.FieldModifier>(this.modifiers.keySet());
            String result = name.getFieldValue(this.fieldID, modifierIDs);
            if (result != null) {
                for (PersonName.FieldModifier modifierID : modifierIDs) {
                    result = this.modifiers.get((Object)modifierID).modifyField(result);
                }
            }
            return result;
        }

        @Override
        public boolean isPopulated(PersonName name) {
            String result = this.format(name);
            return result != null && !result.isEmpty();
        }
    }

    private static class LiteralText
    implements Element {
        private String text;

        public LiteralText(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public String format(PersonName name) {
            return this.text;
        }

        @Override
        public boolean isPopulated(PersonName name) {
            return false;
        }
    }

    private static interface Element {
        public boolean isLiteral();

        public String format(PersonName var1);

        public boolean isPopulated(PersonName var1);
    }
}

