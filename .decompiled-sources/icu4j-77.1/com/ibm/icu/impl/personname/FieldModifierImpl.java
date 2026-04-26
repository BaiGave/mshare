/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.personname;

import com.ibm.icu.impl.personname.PersonNameFormatterImpl;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.CaseMap;
import com.ibm.icu.text.PersonName;
import com.ibm.icu.text.SimpleFormatter;
import java.util.Locale;

abstract class FieldModifierImpl {
    private static final FieldModifierImpl NOOP_MODIFIER = new FieldModifierImpl(){

        @Override
        public String modifyField(String fieldValue) {
            return fieldValue;
        }
    };
    private static final FieldModifierImpl NULL_MODIFIER = new FieldModifierImpl(){

        @Override
        public String modifyField(String fieldValue) {
            return "";
        }
    };
    private static final FieldModifierImpl MONOGRAM_MODIFIER = new FieldModifierImpl(){

        @Override
        public String modifyField(String fieldValue) {
            return FieldModifierImpl.getFirstGrapheme(fieldValue);
        }
    };

    FieldModifierImpl() {
    }

    public abstract String modifyField(String var1);

    public static FieldModifierImpl forName(PersonName.FieldModifier modifierID, PersonNameFormatterImpl formatterImpl) {
        switch (modifierID) {
            case INFORMAL: {
                return NOOP_MODIFIER;
            }
            case PREFIX: {
                return NULL_MODIFIER;
            }
            case CORE: {
                return NOOP_MODIFIER;
            }
            case ALL_CAPS: {
                return new AllCapsModifier(formatterImpl.getLocale());
            }
            case INITIAL_CAP: {
                return new InitialCapModifier(formatterImpl.getLocale());
            }
            case INITIAL: {
                return new InitialModifier(formatterImpl.getLocale(), formatterImpl.getInitialPattern(), formatterImpl.getInitialSequencePattern());
            }
            case RETAIN: {
                return NOOP_MODIFIER;
            }
            case MONOGRAM: {
                return MONOGRAM_MODIFIER;
            }
            case GENITIVE: {
                return NOOP_MODIFIER;
            }
            case VOCATIVE: {
                return NOOP_MODIFIER;
            }
        }
        throw new IllegalArgumentException("Invalid modifier ID " + (Object)((Object)modifierID));
    }

    private static String getFirstGrapheme(String s) {
        if (s.isEmpty()) {
            return "";
        }
        BreakIterator bi = BreakIterator.getCharacterInstance(Locale.ROOT);
        bi.setText(s);
        return s.substring(0, bi.next());
    }

    static class InitialModifier
    extends FieldModifierImpl {
        private final Locale locale;
        private final SimpleFormatter initialFormatter;
        private final SimpleFormatter initialSequenceFormatter;
        private boolean retainPunctuation;

        public InitialModifier(Locale locale, String initialPattern, String initialSequencePattern) {
            this.locale = locale;
            this.initialFormatter = SimpleFormatter.compile(initialPattern);
            this.initialSequenceFormatter = SimpleFormatter.compile(initialSequencePattern);
            this.retainPunctuation = false;
        }

        public void setRetainPunctuation(boolean retain) {
            this.retainPunctuation = retain;
        }

        @Override
        public String modifyField(String fieldValue) {
            String separator = "";
            String result = null;
            BreakIterator bi = BreakIterator.getWordInstance(this.locale);
            bi.setText(fieldValue);
            int wordStart = bi.first();
            int wordEnd = bi.next();
            while (wordEnd != -1) {
                String word = fieldValue.substring(wordStart, wordEnd);
                if (Character.isLetter(word.charAt(0))) {
                    String curInitial = FieldModifierImpl.getFirstGrapheme(word);
                    if (result == null) {
                        result = this.initialFormatter.format(curInitial);
                    } else if (this.retainPunctuation) {
                        result = result + separator + this.initialFormatter.format(curInitial);
                        separator = "";
                    } else {
                        result = this.initialSequenceFormatter.format(result, this.initialFormatter.format(curInitial));
                    }
                } else {
                    separator = Character.isWhitespace(word.charAt(0)) ? separator + word.charAt(0) : separator + word;
                }
                wordStart = wordEnd;
                wordEnd = bi.next();
            }
            return result;
        }
    }

    private static class InitialCapModifier
    extends FieldModifierImpl {
        private final Locale locale;
        private static final CaseMap.Title TO_TITLE_WHOLE_STRING_NO_LOWERCASE = CaseMap.toTitle().wholeString().noLowercase();

        public InitialCapModifier(Locale locale) {
            this.locale = locale;
        }

        @Override
        public String modifyField(String fieldValue) {
            return TO_TITLE_WHOLE_STRING_NO_LOWERCASE.apply(this.locale, null, fieldValue);
        }
    }

    private static class AllCapsModifier
    extends FieldModifierImpl {
        private final Locale locale;

        public AllCapsModifier(Locale locale) {
            this.locale = locale;
        }

        @Override
        public String modifyField(String fieldValue) {
            return UCharacter.toUpperCase(this.locale, fieldValue);
        }
    }
}

