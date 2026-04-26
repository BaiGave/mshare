/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.breakiter;

import com.ibm.icu.impl.Assert;
import com.ibm.icu.impl.CharacterIteration;
import com.ibm.icu.impl.ICUConfig;
import com.ibm.icu.impl.breakiter.DictionaryBreakEngine;
import com.ibm.icu.impl.breakiter.DictionaryData;
import com.ibm.icu.impl.breakiter.DictionaryMatcher;
import com.ibm.icu.impl.breakiter.MlBreakEngine;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.text.UnicodeSetIterator;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceBundleIterator;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashSet;

public class CjkBreakEngine
extends DictionaryBreakEngine {
    private UnicodeSet fHangulWordSet = new UnicodeSet("[\\uac00-\\ud7a3]");
    private UnicodeSet fDigitOrOpenPunctuationOrAlphabetSet;
    private UnicodeSet fClosePunctuationSet;
    private DictionaryMatcher fDictionary = null;
    private HashSet<String> fSkipSet;
    private MlBreakEngine fMlBreakEngine;
    private boolean isCj = false;
    private static final int kMaxKatakanaLength = 8;
    private static final int kMaxKatakanaGroupLength = 20;
    private static final int maxSnlp = 255;
    private static final int kint32max = Integer.MAX_VALUE;

    public CjkBreakEngine(boolean korean) throws IOException {
        this.fHangulWordSet.freeze();
        this.fDigitOrOpenPunctuationOrAlphabetSet = new UnicodeSet("[[:Nd:][:Pi:][:Ps:][:Alphabetic:]]");
        this.fDigitOrOpenPunctuationOrAlphabetSet.freeze();
        this.fClosePunctuationSet = new UnicodeSet("[[:Pc:][:Pd:][:Pe:][:Pf:][:Po:]]");
        this.fClosePunctuationSet.freeze();
        this.fSkipSet = new HashSet();
        this.fDictionary = DictionaryData.loadDictionaryFor("Hira");
        if (korean) {
            this.setCharacters(this.fHangulWordSet);
        } else {
            this.isCj = true;
            UnicodeSet cjSet = new UnicodeSet("[[:Han:][:Hiragana:][:Katakana:]\\u30fc\\uff70\\uff9e\\uff9f]");
            this.setCharacters(cjSet);
            if (Boolean.parseBoolean(ICUConfig.get("com.ibm.icu.impl.breakiter.useMLPhraseBreaking", "false"))) {
                this.fMlBreakEngine = new MlBreakEngine(this.fDigitOrOpenPunctuationOrAlphabetSet, this.fClosePunctuationSet);
            } else {
                this.initializeJapanesePhraseParamater();
            }
        }
    }

    private void initializeJapanesePhraseParamater() {
        this.loadJapaneseExtensions();
        this.loadHiragana();
    }

    private void loadJapaneseExtensions() {
        UResourceBundle rb = UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/brkitr", "ja");
        String tag = "extensions";
        UResourceBundle bundle = rb.get("extensions");
        UResourceBundleIterator iterator = bundle.getIterator();
        while (iterator.hasNext()) {
            this.fSkipSet.add(iterator.nextString());
        }
    }

    private void loadHiragana() {
        UnicodeSet hiraganaWordSet = new UnicodeSet("[:Hiragana:]");
        hiraganaWordSet.freeze();
        UnicodeSetIterator iterator = new UnicodeSetIterator(hiraganaWordSet);
        while (iterator.next()) {
            this.fSkipSet.add(iterator.getString());
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof CjkBreakEngine) {
            CjkBreakEngine other = (CjkBreakEngine)obj;
            return this.fSet.equals(other.fSet);
        }
        return false;
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    private static int getKatakanaCost(int wordlength) {
        int[] katakanaCost = new int[]{8192, 984, 408, 240, 204, 252, 300, 372, 480};
        return wordlength > 8 ? 8192 : katakanaCost[wordlength];
    }

    private static boolean isKatakana(int value) {
        return value >= 12449 && value <= 12542 && value != 12539 || value >= 65382 && value <= 65439;
    }

    @Override
    public int divideUpDictionaryRange(CharacterIterator inText, int startPos, int endPos, DictionaryBreakEngine.DequeI foundBreaks, boolean isPhraseBreaking) {
        StringCharacterIterator text;
        if (startPos >= endPos) {
            return 0;
        }
        inText.setIndex(startPos);
        int inputLength = endPos - startPos;
        int[] charPositions = new int[inputLength + 1];
        StringBuffer s = new StringBuffer("");
        inText.setIndex(startPos);
        while (inText.getIndex() < endPos) {
            s.append(inText.current());
            inText.next();
        }
        String prenormstr = s.toString();
        boolean isNormalized = Normalizer.quickCheck(prenormstr, Normalizer.NFKC) == Normalizer.YES || Normalizer.isNormalized(prenormstr, Normalizer.NFKC, 0);
        int numCodePts = 0;
        if (isNormalized) {
            text = new StringCharacterIterator(prenormstr);
            int index = 0;
            charPositions[0] = 0;
            while (index < prenormstr.length()) {
                int codepoint = prenormstr.codePointAt(index);
                charPositions[++numCodePts] = index += Character.charCount(codepoint);
            }
        } else {
            String normStr = Normalizer.normalize(prenormstr, Normalizer.NFKC);
            text = new StringCharacterIterator(normStr);
            charPositions = new int[normStr.length() + 1];
            Normalizer normalizer = new Normalizer(prenormstr, Normalizer.NFKC, 0);
            int index = 0;
            charPositions[0] = 0;
            while (index < normalizer.endIndex()) {
                normalizer.next();
                charPositions[++numCodePts] = index = normalizer.getIndex();
            }
        }
        if (Boolean.parseBoolean(ICUConfig.get("com.ibm.icu.impl.breakiter.useMLPhraseBreaking", "false")) && isPhraseBreaking && this.isCj) {
            return this.fMlBreakEngine.divideUpRange(inText, startPos, endPos, text, numCodePts, charPositions, foundBreaks);
        }
        int[] bestSnlp = new int[numCodePts + 1];
        bestSnlp[0] = 0;
        for (int i = 1; i <= numCodePts; ++i) {
            bestSnlp[i] = Integer.MAX_VALUE;
        }
        int[] prev = new int[numCodePts + 1];
        for (int i = 0; i <= numCodePts; ++i) {
            prev[i] = -1;
        }
        int maxWordSize = 20;
        int[] values = new int[numCodePts];
        int[] lengths = new int[numCodePts];
        int ix = 0;
        text.setIndex(ix);
        boolean is_prev_katakana = false;
        for (int i = 0; i < numCodePts; ++i) {
            ix = text.getIndex();
            if (bestSnlp[i] != Integer.MAX_VALUE) {
                int maxSearchLength = i + 20 < numCodePts ? 20 : numCodePts - i;
                int[] count_ = new int[1];
                this.fDictionary.matches(text, maxSearchLength, lengths, count_, maxSearchLength, values);
                int count = count_[0];
                text.setIndex(ix);
                if (!(count != 0 && lengths[0] == 1 || CharacterIteration.current32(text) == Integer.MAX_VALUE || this.fHangulWordSet.contains(CharacterIteration.current32(text)))) {
                    values[count] = 255;
                    lengths[count] = 1;
                    ++count;
                }
                for (int j = 0; j < count; ++j) {
                    int newSnlp = bestSnlp[i] + values[j];
                    if (newSnlp >= bestSnlp[lengths[j] + i]) continue;
                    bestSnlp[lengths[j] + i] = newSnlp;
                    prev[lengths[j] + i] = i;
                }
                boolean is_katakana = CjkBreakEngine.isKatakana(CharacterIteration.current32(text));
                if (!is_prev_katakana && is_katakana) {
                    int newSnlp;
                    int j;
                    CharacterIteration.next32(text);
                    for (j = i + 1; j < numCodePts && j - i < 20 && CjkBreakEngine.isKatakana(CharacterIteration.current32(text)); ++j) {
                        CharacterIteration.next32(text);
                    }
                    if (j - i < 20 && (newSnlp = bestSnlp[i] + CjkBreakEngine.getKatakanaCost(j - i)) < bestSnlp[j]) {
                        bestSnlp[j] = newSnlp;
                        prev[j] = i;
                    }
                }
                is_prev_katakana = is_katakana;
            }
            text.setIndex(ix);
            CharacterIteration.next32(text);
        }
        int[] t_boundary = new int[numCodePts + 1];
        int numBreaks = 0;
        if (bestSnlp[numCodePts] == Integer.MAX_VALUE) {
            t_boundary[numBreaks] = numCodePts;
            ++numBreaks;
        } else if (isPhraseBreaking) {
            t_boundary[numBreaks] = numCodePts;
            ++numBreaks;
            int prevIdx = numCodePts;
            int codeUnitIdx = 0;
            int prevCodeUnitIdx = 0;
            int length = 0;
            int i = prev[numCodePts];
            while (i > 0) {
                codeUnitIdx = prenormstr.offsetByCodePoints(0, i);
                prevCodeUnitIdx = prenormstr.offsetByCodePoints(0, prevIdx);
                length = prevCodeUnitIdx - codeUnitIdx;
                prevIdx = i;
                String pattern = this.getPatternFromText(text, s, codeUnitIdx, length);
                text.setIndex(codeUnitIdx);
                if (!(this.fSkipSet.contains(pattern) || CjkBreakEngine.isKatakana(CharacterIteration.current32(text)) && CjkBreakEngine.isKatakana(CharacterIteration.previous32(text)))) {
                    t_boundary[numBreaks] = i;
                    ++numBreaks;
                }
                i = prev[i];
            }
        } else {
            int i = numCodePts;
            while (i > 0) {
                t_boundary[numBreaks] = i;
                ++numBreaks;
                i = prev[i];
            }
            Assert.assrt(prev[t_boundary[numBreaks - 1]] == 0);
        }
        if (foundBreaks.size() == 0 || foundBreaks.peek() < startPos) {
            t_boundary[numBreaks++] = 0;
        }
        int correctedNumBreaks = 0;
        int previous = -1;
        for (int i = numBreaks - 1; i >= 0; --i) {
            int pos = charPositions[t_boundary[i]] + startPos;
            inText.setIndex(pos);
            if (pos > previous && (pos != startPos || isPhraseBreaking && pos > 0 && this.fClosePunctuationSet.contains(CharacterIteration.previous32(inText)))) {
                foundBreaks.push(charPositions[t_boundary[i]] + startPos);
                ++correctedNumBreaks;
            }
            previous = pos;
        }
        if (!foundBreaks.isEmpty() && foundBreaks.peek() == endPos) {
            if (isPhraseBreaking) {
                inText.setIndex(endPos);
                int current = CharacterIteration.current32(inText);
                if (current != Integer.MAX_VALUE && !this.fDigitOrOpenPunctuationOrAlphabetSet.contains(current)) {
                    foundBreaks.pop();
                    --correctedNumBreaks;
                }
            } else {
                foundBreaks.pop();
                --correctedNumBreaks;
            }
        }
        if (!foundBreaks.isEmpty()) {
            inText.setIndex(foundBreaks.peek());
        }
        return correctedNumBreaks;
    }

    private String getPatternFromText(CharacterIterator text, StringBuffer sb, int start, int length) {
        sb.setLength(0);
        if (length > 0) {
            text.setIndex(start);
            sb.append(text.current());
            for (int i = 1; i < length; ++i) {
                sb.append(text.next());
            }
        }
        return sb.toString();
    }
}

