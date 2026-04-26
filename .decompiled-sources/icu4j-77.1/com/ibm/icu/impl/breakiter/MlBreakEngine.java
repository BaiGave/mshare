/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.breakiter;

import com.ibm.icu.impl.CharacterIteration;
import com.ibm.icu.impl.breakiter.DictionaryBreakEngine;
import com.ibm.icu.impl.breakiter.ModelIndex;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceBundleIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MlBreakEngine {
    private static final int MAX_FEATURE = 13;
    private UnicodeSet fDigitOrOpenPunctuationOrAlphabetSet;
    private UnicodeSet fClosePunctuationSet;
    private List<HashMap<String, Integer>> fModel;
    private int fNegativeSum;

    public MlBreakEngine(UnicodeSet digitOrOpenPunctuationOrAlphabetSet, UnicodeSet closePunctuationSet) {
        this.fDigitOrOpenPunctuationOrAlphabetSet = digitOrOpenPunctuationOrAlphabetSet;
        this.fClosePunctuationSet = closePunctuationSet;
        this.fModel = new ArrayList<HashMap<String, Integer>>(13);
        for (int i = 0; i < 13; ++i) {
            this.fModel.add(new HashMap());
        }
        this.fNegativeSum = 0;
        this.loadMLModel();
    }

    public int divideUpRange(CharacterIterator inText, int startPos, int endPos, CharacterIterator inString, int codePointLength, int[] charPositions, DictionaryBreakEngine.DequeI foundBreaks) {
        if (startPos >= endPos) {
            return 0;
        }
        ArrayList<Integer> boundary = new ArrayList<Integer>(codePointLength);
        String inputStr = this.transform(inString);
        int indexSize = codePointLength + 4;
        int[] indexList = new int[indexSize];
        int numCodeUnits = this.initIndexList(inString, indexList, codePointLength);
        boundary.add(0, 0);
        int idx = 0;
        while (idx + 1 < codePointLength) {
            this.evaluateBreakpoint(inputStr, indexList, idx, numCodeUnits, boundary);
            if (idx + 4 < codePointLength) {
                indexList[idx + 6] = numCodeUnits;
                numCodeUnits += Character.charCount(CharacterIteration.next32(inString));
            }
            ++idx;
        }
        if ((Integer)boundary.get(boundary.size() - 1) != codePointLength) {
            boundary.add(codePointLength);
        }
        int correctedNumBreaks = 0;
        int previous = -1;
        int numBreaks = boundary.size();
        for (int i = 0; i < numBreaks; ++i) {
            int pos = charPositions[boundary.get(i)] + startPos;
            inText.setIndex(pos);
            if (pos > previous && (pos != startPos || pos > 0 && this.fClosePunctuationSet.contains(CharacterIteration.previous32(inText)))) {
                foundBreaks.push(pos);
                ++correctedNumBreaks;
            }
            previous = pos;
        }
        if (!foundBreaks.isEmpty() && foundBreaks.peek() == endPos) {
            inText.setIndex(endPos);
            int current = CharacterIteration.current32(inText);
            if (current != Integer.MAX_VALUE && !this.fDigitOrOpenPunctuationOrAlphabetSet.contains(current)) {
                foundBreaks.pop();
                --correctedNumBreaks;
            }
        }
        if (!foundBreaks.isEmpty()) {
            inText.setIndex(foundBreaks.peek());
        }
        return correctedNumBreaks;
    }

    private String transform(CharacterIterator inString) {
        StringBuilder sb = new StringBuilder();
        inString.setIndex(0);
        char c = inString.first();
        while (c != '\uffff') {
            sb.append(c);
            c = inString.next();
        }
        return sb.toString();
    }

    private void evaluateBreakpoint(String inputStr, int[] indexList, int startIdx, int numCodeUnits, ArrayList<Integer> boundary) {
        int i;
        int start = 0;
        int end = 0;
        int score = this.fNegativeSum;
        for (i = 0; i < 6; ++i) {
            start = startIdx + i;
            if (indexList[start] == -1) continue;
            end = indexList[start + 1] != -1 ? indexList[start + 1] : numCodeUnits;
            score += this.fModel.get(ModelIndex.kUWStart.getValue() + i).getOrDefault(inputStr.substring(indexList[start], end), 0).intValue();
        }
        for (i = 0; i < 3; ++i) {
            start = startIdx + i + 1;
            if (indexList[start] == -1 || indexList[start + 1] == -1) continue;
            end = indexList[start + 2] != -1 ? indexList[start + 2] : numCodeUnits;
            score += this.fModel.get(ModelIndex.kBWStart.getValue() + i).getOrDefault(inputStr.substring(indexList[start], end), 0).intValue();
        }
        for (i = 0; i < 4; ++i) {
            start = startIdx + i;
            if (indexList[start] == -1 || indexList[start + 1] == -1 || indexList[start + 2] == -1) continue;
            end = indexList[start + 3] != -1 ? indexList[start + 3] : numCodeUnits;
            score += this.fModel.get(ModelIndex.kTWStart.getValue() + i).getOrDefault(inputStr.substring(indexList[start], end), 0).intValue();
        }
        if (score > 0) {
            boundary.add(startIdx + 1);
        }
    }

    private int initIndexList(CharacterIterator inString, int[] indexList, int codePointLength) {
        int index = 0;
        inString.setIndex(index);
        Arrays.fill(indexList, -1);
        if (codePointLength > 0) {
            indexList[2] = 0;
            index += Character.charCount(CharacterIteration.current32(inString));
            if (codePointLength > 1) {
                indexList[3] = index;
                index += Character.charCount(CharacterIteration.next32(inString));
                if (codePointLength > 2) {
                    indexList[4] = index;
                    index += Character.charCount(CharacterIteration.next32(inString));
                    if (codePointLength > 3) {
                        indexList[5] = index;
                        index += Character.charCount(CharacterIteration.next32(inString));
                    }
                }
            }
        }
        return index;
    }

    private void loadMLModel() {
        int index = 0;
        UResourceBundle rb = UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/brkitr", "jaml");
        this.initKeyValue(rb, "UW1Keys", "UW1Values", this.fModel.get(index++));
        this.initKeyValue(rb, "UW2Keys", "UW2Values", this.fModel.get(index++));
        this.initKeyValue(rb, "UW3Keys", "UW3Values", this.fModel.get(index++));
        this.initKeyValue(rb, "UW4Keys", "UW4Values", this.fModel.get(index++));
        this.initKeyValue(rb, "UW5Keys", "UW5Values", this.fModel.get(index++));
        this.initKeyValue(rb, "UW6Keys", "UW6Values", this.fModel.get(index++));
        this.initKeyValue(rb, "BW1Keys", "BW1Values", this.fModel.get(index++));
        this.initKeyValue(rb, "BW2Keys", "BW2Values", this.fModel.get(index++));
        this.initKeyValue(rb, "BW3Keys", "BW3Values", this.fModel.get(index++));
        this.initKeyValue(rb, "TW1Keys", "TW1Values", this.fModel.get(index++));
        this.initKeyValue(rb, "TW2Keys", "TW2Values", this.fModel.get(index++));
        this.initKeyValue(rb, "TW3Keys", "TW3Values", this.fModel.get(index++));
        this.initKeyValue(rb, "TW4Keys", "TW4Values", this.fModel.get(index++));
        this.fNegativeSum /= 2;
    }

    private void initKeyValue(UResourceBundle rb, String keyName, String valueName, HashMap<String, Integer> map) {
        int idx = 0;
        UResourceBundle keyBundle = rb.get(keyName);
        UResourceBundle valueBundle = rb.get(valueName);
        int[] value = valueBundle.getIntVector();
        UResourceBundleIterator iterator = keyBundle.getIterator();
        while (iterator.hasNext()) {
            this.fNegativeSum -= value[idx];
            map.put(iterator.nextString(), value[idx++]);
        }
    }
}

