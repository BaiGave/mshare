/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import java.util.Locale;
import java.util.Set;

public interface PersonName {
    public Locale getNameLocale();

    public PreferredOrder getPreferredOrder();

    public String getFieldValue(NameField var1, Set<FieldModifier> var2);

    public static enum PreferredOrder {
        DEFAULT,
        GIVEN_FIRST,
        SURNAME_FIRST;

    }

    public static enum FieldModifier {
        INFORMAL("informal"),
        PREFIX("prefix"),
        CORE("core"),
        INITIAL("initial"),
        MONOGRAM("monogram"),
        ALL_CAPS("allCaps"),
        RETAIN("retain"),
        INITIAL_CAP("initialCap"),
        GENITIVE("genitive"),
        VOCATIVE("vocative");

        private final String name;

        private FieldModifier(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public static FieldModifier forString(String name) {
            for (FieldModifier modifier : FieldModifier.values()) {
                if (!modifier.name.equals(name)) continue;
                return modifier;
            }
            throw new IllegalArgumentException("Invalid modifier name " + name);
        }
    }

    public static enum NameField {
        TITLE("title"),
        GIVEN("given"),
        GIVEN2("given2"),
        SURNAME("surname"),
        SURNAME2("surname2"),
        GENERATION("generation"),
        CREDENTIALS("credentials");

        private final String name;

        private NameField(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        @Deprecated
        public static NameField forString(String name) {
            for (NameField field : NameField.values()) {
                if (!field.name.equals(name)) continue;
                return field;
            }
            throw new IllegalArgumentException("Invalid field name " + name);
        }
    }
}

