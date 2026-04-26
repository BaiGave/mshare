/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.Resources;
import org.apache.commons.codec.StringEncoder;

public class DaitchMokotoffSoundex
implements StringEncoder {
    private static final char NUL = '\u0000';
    private static final String COMMENT = "//";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String MULTILINE_COMMENT_END = "*/";
    private static final String MULTILINE_COMMENT_START = "/*";
    private static final String RESOURCE_FILE = "/org/apache/commons/codec/language/dmrules.txt";
    private static final int MAX_LENGTH = 6;
    private static final Map<Character, List<Rule>> RULES = new HashMap<Character, List<Rule>>();
    private static final Map<Character, Character> FOLDINGS = new HashMap<Character, Character>();
    private static final Pattern EQUAL = Pattern.compile("=");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private final boolean folding;

    private static void parseRules(Scanner scanner, String location, Map<Character, List<Rule>> ruleMapping, Map<Character, Character> asciiFoldings) {
        int currentLine = 0;
        boolean inMultilineComment = false;
        while (scanner.hasNextLine()) {
            String[] parts;
            String rawLine;
            ++currentLine;
            String line = rawLine = scanner.nextLine();
            if (inMultilineComment) {
                if (!line.endsWith(MULTILINE_COMMENT_END)) continue;
                inMultilineComment = false;
                continue;
            }
            if (line.startsWith(MULTILINE_COMMENT_START)) {
                inMultilineComment = true;
                continue;
            }
            int cmtI = line.indexOf(COMMENT);
            if (cmtI >= 0) {
                line = line.substring(0, cmtI);
            }
            if ((line = line.trim()).isEmpty()) continue;
            if (line.contains("=")) {
                parts = EQUAL.split(line);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Malformed folding statement split into " + parts.length + " parts: " + rawLine + " in " + location);
                }
                String leftCharacter = parts[0];
                String rightCharacter = parts[1];
                if (leftCharacter.length() != 1 || rightCharacter.length() != 1) {
                    throw new IllegalArgumentException("Malformed folding statement - patterns are not single characters: " + rawLine + " in " + location);
                }
                asciiFoldings.put(Character.valueOf(leftCharacter.charAt(0)), Character.valueOf(rightCharacter.charAt(0)));
                continue;
            }
            parts = SPACES.split(line);
            if (parts.length != 4) {
                throw new IllegalArgumentException("Malformed rule statement split into " + parts.length + " parts: " + rawLine + " in " + location);
            }
            try {
                String pattern = DaitchMokotoffSoundex.stripQuotes(parts[0]);
                String replacement1 = DaitchMokotoffSoundex.stripQuotes(parts[1]);
                String replacement2 = DaitchMokotoffSoundex.stripQuotes(parts[2]);
                String replacement3 = DaitchMokotoffSoundex.stripQuotes(parts[3]);
                Rule r = new Rule(pattern, replacement1, replacement2, replacement3);
                char patternKey = r.pattern.charAt(0);
                List rules = ruleMapping.computeIfAbsent(Character.valueOf(patternKey), k -> new ArrayList());
                rules.add(r);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Problem parsing line '" + currentLine + "' in " + location, e);
            }
        }
    }

    private static String stripQuotes(String str) {
        if (str.startsWith(DOUBLE_QUOTE)) {
            str = str.substring(1);
        }
        if (str.endsWith(DOUBLE_QUOTE)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public DaitchMokotoffSoundex() {
        this(true);
    }

    public DaitchMokotoffSoundex(boolean folding) {
        this.folding = folding;
    }

    private String cleanup(String input) {
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch) || !Character.isLetter(ch)) continue;
            ch = Character.toLowerCase(ch);
            Character character = FOLDINGS.get(Character.valueOf(ch));
            if (this.folding && character != null) {
                ch = character.charValue();
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    @Override
    public Object encode(Object obj) throws EncoderException {
        if (!(obj instanceof String)) {
            throw new EncoderException("Parameter supplied to DaitchMokotoffSoundex encode is not of type java.lang.String");
        }
        return this.encode((String)obj);
    }

    @Override
    public String encode(String source) {
        if (source == null) {
            return null;
        }
        return this.soundex(source, false)[0];
    }

    public String soundex(String source) {
        return String.join((CharSequence)"|", this.soundex(source, true));
    }

    private String[] soundex(String source, boolean branching) {
        if (source == null) {
            return null;
        }
        String input = this.cleanup(source);
        LinkedHashSet<Branch> currentBranches = new LinkedHashSet<Branch>();
        currentBranches.add(new Branch());
        char lastChar = '\u0000';
        for (int index = 0; index < input.length(); ++index) {
            char ch = input.charAt(index);
            String inputContext = input.substring(index);
            List<Rule> rules = RULES.get(Character.valueOf(ch));
            if (rules == null) continue;
            ArrayList<Branch> nextBranches = branching ? new ArrayList<Branch>() : Collections.emptyList();
            for (Rule rule : rules) {
                if (!rule.matches(inputContext)) continue;
                if (branching) {
                    nextBranches.clear();
                }
                String[] replacements = rule.getReplacements(inputContext, lastChar == '\u0000');
                boolean branchingRequired = replacements.length > 1 && branching;
                block2: for (Branch branch : currentBranches) {
                    for (String nextReplacement : replacements) {
                        Branch nextBranch = branchingRequired ? branch.createBranch() : branch;
                        boolean force = lastChar == 'm' && ch == 'n' || lastChar == 'n' && ch == 'm';
                        nextBranch.processNextReplacement(nextReplacement, force);
                        if (!branching) continue block2;
                        nextBranches.add(nextBranch);
                    }
                }
                if (branching) {
                    currentBranches.clear();
                    currentBranches.addAll(nextBranches);
                }
                index += rule.getPatternLength() - 1;
                break;
            }
            lastChar = ch;
        }
        String[] result = new String[currentBranches.size()];
        int index = 0;
        for (Branch branch : currentBranches) {
            branch.finish();
            result[index++] = branch.toString();
        }
        return result;
    }

    static {
        try (Scanner scanner = new Scanner(Resources.getInputStream(RESOURCE_FILE), CharEncoding.UTF_8);){
            DaitchMokotoffSoundex.parseRules(scanner, RESOURCE_FILE, RULES, FOLDINGS);
        }
        RULES.forEach((k, v) -> v.sort((rule1, rule2) -> ((Rule)rule2).getPatternLength() - ((Rule)rule1).getPatternLength()));
    }

    private static final class Rule {
        private static final Pattern PIPE = Pattern.compile("\\|");
        private final String pattern;
        private final String[] replacementAtStart;
        private final String[] replacementBeforeVowel;
        private final String[] replacementDefault;

        private Rule(String pattern, String replacementAtStart, String replacementBeforeVowel, String replacementDefault) {
            this.pattern = pattern;
            this.replacementAtStart = PIPE.split(replacementAtStart);
            this.replacementBeforeVowel = PIPE.split(replacementBeforeVowel);
            this.replacementDefault = PIPE.split(replacementDefault);
        }

        private int getPatternLength() {
            return this.pattern.length();
        }

        private String[] getReplacements(String context, boolean atStart) {
            boolean nextCharIsVowel;
            if (atStart) {
                return this.replacementAtStart;
            }
            int nextIndex = this.getPatternLength();
            boolean bl = nextCharIsVowel = nextIndex < context.length() && this.isVowel(context.charAt(nextIndex));
            if (nextCharIsVowel) {
                return this.replacementBeforeVowel;
            }
            return this.replacementDefault;
        }

        private boolean isVowel(char ch) {
            return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
        }

        private boolean matches(String context) {
            return context.startsWith(this.pattern);
        }

        public String toString() {
            return String.format("%s=(%s,%s,%s)", this.pattern, Arrays.asList(this.replacementAtStart), Arrays.asList(this.replacementBeforeVowel), Arrays.asList(this.replacementDefault));
        }
    }

    private static final class Branch {
        private final StringBuilder builder = new StringBuilder();
        private String cachedString;
        private String lastReplacement;

        private Branch() {
        }

        private Branch createBranch() {
            Branch branch = new Branch();
            branch.builder.append(this.toString());
            branch.lastReplacement = this.lastReplacement;
            return branch;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Branch)) {
                return false;
            }
            return this.toString().equals(((Branch)other).toString());
        }

        private void finish() {
            while (this.builder.length() < 6) {
                this.builder.append('0');
                this.cachedString = null;
            }
        }

        public int hashCode() {
            return this.toString().hashCode();
        }

        private void processNextReplacement(String replacement, boolean forceAppend) {
            boolean append;
            boolean bl = append = this.lastReplacement == null || !this.lastReplacement.endsWith(replacement) || forceAppend;
            if (append && this.builder.length() < 6) {
                this.builder.append(replacement);
                if (this.builder.length() > 6) {
                    this.builder.delete(6, this.builder.length());
                }
                this.cachedString = null;
            }
            this.lastReplacement = replacement;
        }

        public String toString() {
            if (this.cachedString == null) {
                this.cachedString = this.builder.toString();
            }
            return this.cachedString;
        }
    }
}

