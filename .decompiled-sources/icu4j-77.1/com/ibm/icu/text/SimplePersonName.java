/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.text.PersonName;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class SimplePersonName
implements PersonName {
    private final Locale nameLocale;
    private final PersonName.PreferredOrder preferredOrder;
    private final Map<String, String> fieldValues;

    public static Builder builder() {
        return new Builder();
    }

    private SimplePersonName(Locale nameLocale, PersonName.PreferredOrder preferredOrder, Map<String, String> fieldValues) {
        this.nameLocale = nameLocale;
        this.preferredOrder = preferredOrder;
        this.fieldValues = new HashMap<String, String>(fieldValues);
    }

    @Override
    public Locale getNameLocale() {
        return this.nameLocale;
    }

    @Override
    public PersonName.PreferredOrder getPreferredOrder() {
        return this.preferredOrder;
    }

    @Override
    public String getFieldValue(PersonName.NameField nameField, Set<PersonName.FieldModifier> modifiers) {
        String fieldName = nameField.toString();
        String result = this.fieldValues.get(SimplePersonName.makeModifiedFieldName(nameField, modifiers));
        if (result != null) {
            modifiers.clear();
            return result;
        }
        result = this.fieldValues.get(fieldName);
        if (result == null) {
            return null;
        }
        if (modifiers.size() == 1) {
            return result;
        }
        String winningKey = fieldName;
        int winningScore = 0;
        for (String key : this.fieldValues.keySet()) {
            Set<PersonName.FieldModifier> keyModifiers;
            if (!key.startsWith(fieldName) || !modifiers.containsAll(keyModifiers = SimplePersonName.makeModifiersFromName(key)) || keyModifiers.size() <= winningScore && (keyModifiers.size() != winningScore || key.compareTo(winningKey) >= 0)) continue;
            winningKey = key;
            winningScore = keyModifiers.size();
        }
        result = this.fieldValues.get(winningKey);
        modifiers.removeAll(SimplePersonName.makeModifiersFromName(winningKey));
        return result;
    }

    @Deprecated
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : this.fieldValues.keySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key + "=" + this.fieldValues.get(key));
        }
        sb.append(",locale=" + this.nameLocale);
        return sb.toString();
    }

    private static String makeModifiedFieldName(PersonName.NameField fieldName, Collection<PersonName.FieldModifier> modifiers) {
        StringBuilder result = new StringBuilder();
        result.append((Object)fieldName);
        TreeSet<String> sortedModifierNames = new TreeSet<String>();
        for (PersonName.FieldModifier modifier : modifiers) {
            sortedModifierNames.add(modifier.toString());
        }
        for (String modifierName : sortedModifierNames) {
            result.append("-");
            result.append(modifierName);
        }
        return result.toString();
    }

    private static Set<PersonName.FieldModifier> makeModifiersFromName(String modifiedName) {
        StringTokenizer tok = new StringTokenizer(modifiedName, "-");
        HashSet<PersonName.FieldModifier> result = new HashSet<PersonName.FieldModifier>();
        String fieldName = tok.nextToken();
        while (tok.hasMoreTokens()) {
            result.add(PersonName.FieldModifier.forString(tok.nextToken()));
        }
        return result;
    }

    public static class Builder {
        private Locale locale = null;
        private PersonName.PreferredOrder preferredOrder = PersonName.PreferredOrder.DEFAULT;
        private Map<String, String> fieldValues = new HashMap<String, String>();

        public Builder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder setPreferredOrder(PersonName.PreferredOrder preferredOrder) {
            this.preferredOrder = preferredOrder;
            return this;
        }

        public Builder addField(PersonName.NameField field, Collection<PersonName.FieldModifier> modifiers, String value) {
            TreeSet<String> modifierNames = new TreeSet<String>();
            if (modifiers != null) {
                for (PersonName.FieldModifier modifier : modifiers) {
                    modifierNames.add(modifier.toString());
                }
            }
            StringBuilder fieldName = new StringBuilder();
            fieldName.append(field.toString());
            for (String modifierName : modifierNames) {
                fieldName.append("-");
                fieldName.append(modifierName);
            }
            this.fieldValues.put(fieldName.toString(), value);
            return this;
        }

        public SimplePersonName build() {
            if (this.fieldValues.get("surname") == null) {
                String surnamePrefix = this.fieldValues.get("surname-prefix");
                String surnameCore = this.fieldValues.get("surname-core");
                if (surnamePrefix != null && surnameCore != null) {
                    this.fieldValues.put("surname", surnamePrefix + " " + surnameCore);
                } else if (surnamePrefix != null) {
                    this.fieldValues.put("surname", surnamePrefix);
                } else if (surnameCore != null) {
                    this.fieldValues.put("surname", surnameCore);
                }
            }
            return new SimplePersonName(this.locale, this.preferredOrder, this.fieldValues);
        }

        private Builder() {
        }
    }
}

