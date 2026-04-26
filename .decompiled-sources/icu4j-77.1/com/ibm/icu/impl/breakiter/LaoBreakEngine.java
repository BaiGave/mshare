/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.breakiter;

import com.ibm.icu.impl.breakiter.DictionaryBreakEngine;
import com.ibm.icu.impl.breakiter.DictionaryData;
import com.ibm.icu.impl.breakiter.DictionaryMatcher;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UnicodeSet;
import java.io.IOException;
import java.text.CharacterIterator;

public class LaoBreakEngine
extends DictionaryBreakEngine {
    private static final byte LAO_LOOKAHEAD = 3;
    private static final byte LAO_ROOT_COMBINE_THRESHOLD = 3;
    private static final byte LAO_PREFIX_COMBINE_THRESHOLD = 3;
    private static final byte LAO_MIN_WORD = 2;
    private DictionaryMatcher fDictionary;
    private UnicodeSet fEndWordSet;
    private UnicodeSet fBeginWordSet;
    private UnicodeSet fMarkSet;

    public LaoBreakEngine() throws IOException {
        UnicodeSet laoWordSet = new UnicodeSet("[[:Laoo:]&[:LineBreak=SA:]]");
        this.fMarkSet = new UnicodeSet("[[:Laoo:]&[:LineBreak=SA:]&[:M:]]");
        this.fMarkSet.add(32);
        this.fBeginWordSet = new UnicodeSet(3713, 3758, 3776, 3780, 3804, 3805);
        laoWordSet.compact();
        this.fEndWordSet = new UnicodeSet(laoWordSet);
        this.fEndWordSet.remove(3776, 3780);
        this.fMarkSet.compact();
        this.fEndWordSet.compact();
        this.fBeginWordSet.compact();
        laoWordSet.freeze();
        this.fMarkSet.freeze();
        this.fEndWordSet.freeze();
        this.fBeginWordSet.freeze();
        this.setCharacters(laoWordSet);
        this.fDictionary = DictionaryData.loadDictionaryFor("Laoo");
    }

    public boolean equals(Object obj) {
        return obj instanceof LaoBreakEngine;
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean handles(int c) {
        int script = UCharacter.getIntPropertyValue(c, 4106);
        return script == 24;
    }

    @Override
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DictionaryBreakEngine.DequeI foundBreaks, boolean isPhraseBreaking) {
        int current;
        if (rangeEnd - rangeStart < 2) {
            return 0;
        }
        int wordsFound = 0;
        DictionaryBreakEngine.PossibleWord[] words = new DictionaryBreakEngine.PossibleWord[3];
        for (int i = 0; i < 3; ++i) {
            words[i] = new DictionaryBreakEngine.PossibleWord();
        }
        fIter.setIndex(rangeStart);
        while ((current = fIter.getIndex()) < rangeEnd) {
            int currPos;
            int wordLength = 0;
            int candidates = words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd);
            if (candidates == 1) {
                wordLength = words[wordsFound % 3].acceptMarked(fIter);
                ++wordsFound;
            } else if (candidates > 1) {
                boolean foundBest = false;
                if (fIter.getIndex() < rangeEnd) {
                    block2: do {
                        if (words[(wordsFound + 1) % 3].candidates(fIter, this.fDictionary, rangeEnd) <= 0) continue;
                        words[wordsFound % 3].markCurrent();
                        if (fIter.getIndex() >= rangeEnd) break;
                        do {
                            if (words[(wordsFound + 2) % 3].candidates(fIter, this.fDictionary, rangeEnd) <= 0) continue;
                            words[wordsFound % 3].markCurrent();
                            foundBest = true;
                            continue block2;
                        } while (words[(wordsFound + 1) % 3].backUp(fIter));
                    } while (words[wordsFound % 3].backUp(fIter) && !foundBest);
                }
                wordLength = words[wordsFound % 3].acceptMarked(fIter);
                ++wordsFound;
            }
            if (fIter.getIndex() < rangeEnd && wordLength < 3) {
                if (words[wordsFound % 3].candidates(fIter, this.fDictionary, rangeEnd) <= 0 && (wordLength == 0 || words[wordsFound % 3].longestPrefix() < 3)) {
                    int remaining = rangeEnd - (current + wordLength);
                    char pc = fIter.current();
                    int chars = 0;
                    while (true) {
                        fIter.next();
                        char uc = fIter.current();
                        ++chars;
                        if (--remaining <= 0) break;
                        if (this.fEndWordSet.contains(pc) && this.fBeginWordSet.contains(uc)) {
                            int candidate = words[(wordsFound + 1) % 3].candidates(fIter, this.fDictionary, rangeEnd);
                            fIter.setIndex(current + wordLength + chars);
                            if (candidate > 0) break;
                        }
                        pc = uc;
                    }
                    if (wordLength <= 0) {
                        ++wordsFound;
                    }
                    wordLength += chars;
                } else {
                    fIter.setIndex(current + wordLength);
                }
            }
            while ((currPos = fIter.getIndex()) < rangeEnd && this.fMarkSet.contains(fIter.current())) {
                fIter.next();
                wordLength += fIter.getIndex() - currPos;
            }
            if (wordLength <= 0) continue;
            foundBreaks.push(current + wordLength);
        }
        if (foundBreaks.peek() >= rangeEnd) {
            foundBreaks.pop();
            --wordsFound;
        }
        return wordsFound;
    }
}

