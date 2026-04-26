/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.Assert;
import com.ibm.icu.text.RBBINode;
import com.ibm.icu.text.RBBIRuleBuilder;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.CodePointTrie;
import com.ibm.icu.util.MutableCodePointTrie;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class RBBISetBuilder {
    RBBIRuleBuilder fRB;
    RangeDescriptor fRangeList;
    MutableCodePointTrie fTrie;
    CodePointTrie fFrozenTrie;
    int fGroupCount;
    int fDictCategoriesStart;
    boolean fSawBOF;
    private static final int MAX_CHAR_CATEGORIES_FOR_8BITS_TRIE = 255;

    RBBISetBuilder(RBBIRuleBuilder rb) {
        this.fRB = rb;
    }

    void buildRanges() {
        RangeDescriptor rlRange;
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("usets") >= 0) {
            this.printSets();
        }
        this.fRangeList = new RangeDescriptor();
        this.fRangeList.fStartChar = 0;
        this.fRangeList.fEndChar = 0x10FFFF;
        for (RBBINode usetNode : this.fRB.fUSetNodes) {
            UnicodeSet inputSet = usetNode.fInputSet;
            int inputSetRangeCount = inputSet.getRangeCount();
            int inputSetRangeIndex = 0;
            rlRange = this.fRangeList;
            while (inputSetRangeIndex < inputSetRangeCount) {
                int inputSetRangeBegin = inputSet.getRangeStart(inputSetRangeIndex);
                int inputSetRangeEnd = inputSet.getRangeEnd(inputSetRangeIndex);
                while (rlRange.fEndChar < inputSetRangeBegin) {
                    rlRange = rlRange.fNext;
                }
                if (rlRange.fStartChar < inputSetRangeBegin) {
                    rlRange.split(inputSetRangeBegin);
                    continue;
                }
                if (rlRange.fEndChar > inputSetRangeEnd) {
                    rlRange.split(inputSetRangeEnd + 1);
                }
                if (rlRange.fIncludesSets.indexOf(usetNode) == -1) {
                    rlRange.fIncludesSets.add(usetNode);
                }
                if (inputSetRangeEnd == rlRange.fEndChar) {
                    ++inputSetRangeIndex;
                }
                rlRange = rlRange.fNext;
            }
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("range") >= 0) {
            this.printRanges();
        }
        int dictGroupCount = 0;
        rlRange = this.fRangeList;
        while (rlRange != null) {
            RangeDescriptor rlSearchRange = this.fRangeList;
            while (rlSearchRange != rlRange) {
                if (rlRange.fIncludesSets.equals(rlSearchRange.fIncludesSets)) {
                    rlRange.fNum = rlSearchRange.fNum;
                    rlRange.fIncludesDict = rlSearchRange.fIncludesDict;
                    break;
                }
                rlSearchRange = rlSearchRange.fNext;
            }
            if (rlRange.fNum == 0) {
                rlRange.fFirstInGroup = true;
                if (rlRange.isDictionaryRange()) {
                    rlRange.fNum = ++dictGroupCount;
                    rlRange.fIncludesDict = true;
                } else {
                    ++this.fGroupCount;
                    rlRange.fNum = this.fGroupCount + 2;
                    this.addValToSets(rlRange.fIncludesSets, this.fGroupCount + 2);
                }
            }
            rlRange = rlRange.fNext;
        }
        this.fDictCategoriesStart = this.fGroupCount + 3;
        rlRange = this.fRangeList;
        while (rlRange != null) {
            if (rlRange.fIncludesDict) {
                rlRange.fNum += this.fDictCategoriesStart - 1;
                if (rlRange.fFirstInGroup) {
                    this.addValToSets(rlRange.fIncludesSets, rlRange.fNum);
                }
            }
            rlRange = rlRange.fNext;
        }
        this.fGroupCount += dictGroupCount;
        String eofString = "eof";
        String bofString = "bof";
        for (RBBINode usetNode : this.fRB.fUSetNodes) {
            UnicodeSet inputSet = usetNode.fInputSet;
            if (inputSet.contains(eofString)) {
                this.addValToSet(usetNode, 1);
            }
            if (!inputSet.contains(bofString)) continue;
            this.addValToSet(usetNode, 2);
            this.fSawBOF = true;
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("rgroup") >= 0) {
            this.printRangeGroups();
        }
        if (this.fRB.fDebugEnv != null && this.fRB.fDebugEnv.indexOf("esets") >= 0) {
            this.printSets();
        }
    }

    void buildTrie() {
        this.fTrie = new MutableCodePointTrie(0, 0);
        RangeDescriptor rlRange = this.fRangeList;
        while (rlRange != null) {
            this.fTrie.setRange(rlRange.fStartChar, rlRange.fEndChar, rlRange.fNum);
            rlRange = rlRange.fNext;
        }
    }

    void mergeCategories(RBBIRuleBuilder.IntPair categories) {
        assert (categories.first >= 1);
        assert (categories.second > categories.first);
        assert (categories.first < this.fDictCategoriesStart && categories.second < this.fDictCategoriesStart || categories.first >= this.fDictCategoriesStart && categories.second >= this.fDictCategoriesStart);
        RangeDescriptor rd = this.fRangeList;
        while (rd != null) {
            int rangeNum;
            if ((rangeNum = rd.fNum--) == categories.second) {
                rd.fNum = categories.first;
            } else if (rangeNum > categories.second) {
                // empty if block
            }
            rd = rd.fNext;
        }
        --this.fGroupCount;
        if (categories.second <= this.fDictCategoriesStart) {
            --this.fDictCategoriesStart;
        }
    }

    void freezeTrieIfNotYet() {
        if (this.fFrozenTrie == null) {
            boolean use8Bits = this.getNumCharCategories() <= 255;
            this.fFrozenTrie = this.fTrie.buildImmutable(CodePointTrie.Type.FAST, use8Bits ? CodePointTrie.ValueWidth.BITS_8 : CodePointTrie.ValueWidth.BITS_16);
            this.fTrie = null;
        }
    }

    int getTrieSize() {
        this.freezeTrieIfNotYet();
        return this.fFrozenTrie.toBinary(new ByteArrayOutputStream());
    }

    void serializeTrie(OutputStream os) throws IOException {
        this.freezeTrieIfNotYet();
        this.fFrozenTrie.toBinary(os);
    }

    void addValToSets(List<RBBINode> sets, int val) {
        for (RBBINode usetNode : sets) {
            this.addValToSet(usetNode, val);
        }
    }

    void addValToSet(RBBINode usetNode, int val) {
        RBBINode leafNode = new RBBINode(3);
        leafNode.fVal = val;
        if (usetNode.fLeftChild == null) {
            usetNode.fLeftChild = leafNode;
            leafNode.fParent = usetNode;
        } else {
            RBBINode orNode = new RBBINode(9);
            orNode.fLeftChild = usetNode.fLeftChild;
            orNode.fRightChild = leafNode;
            orNode.fLeftChild.fParent = orNode;
            orNode.fRightChild.fParent = orNode;
            usetNode.fLeftChild = orNode;
            orNode.fParent = usetNode;
        }
    }

    int getNumCharCategories() {
        return this.fGroupCount + 3;
    }

    int getDictCategoriesStart() {
        return this.fDictCategoriesStart;
    }

    boolean sawBOF() {
        return this.fSawBOF;
    }

    int getFirstChar(int category) {
        int retVal = -1;
        RangeDescriptor rlRange = this.fRangeList;
        while (rlRange != null) {
            if (rlRange.fNum == category) {
                retVal = rlRange.fStartChar;
                break;
            }
            rlRange = rlRange.fNext;
        }
        return retVal;
    }

    void printRanges() {
        System.out.print("\n\n Nonoverlapping Ranges ...\n");
        RangeDescriptor rlRange = this.fRangeList;
        while (rlRange != null) {
            System.out.printf("%04x-%04x ", rlRange.fStartChar, rlRange.fEndChar);
            for (int i = 0; i < rlRange.fIncludesSets.size(); ++i) {
                RBBINode varRef;
                RBBINode usetNode = rlRange.fIncludesSets.get(i);
                String setName = "anon";
                RBBINode setRef = usetNode.fParent;
                if (setRef != null && (varRef = setRef.fParent) != null && varRef.fType == 2) {
                    setName = varRef.fText;
                }
                System.out.print(setName);
                System.out.print("  ");
            }
            System.out.println("");
            rlRange = rlRange.fNext;
        }
    }

    void printRangeGroups() {
        System.out.print("\nRanges grouped by Unicode Set Membership...\n");
        RangeDescriptor rlRange = this.fRangeList;
        while (rlRange != null) {
            if (rlRange.fFirstInGroup) {
                int i;
                int groupNum = rlRange.fNum;
                if (groupNum < 10) {
                    System.out.print(" ");
                }
                System.out.print(groupNum + " ");
                if (groupNum >= this.fDictCategoriesStart) {
                    System.out.print(" <DICT> ");
                }
                for (i = 0; i < rlRange.fIncludesSets.size(); ++i) {
                    RBBINode varRef;
                    RBBINode usetNode = rlRange.fIncludesSets.get(i);
                    String setName = "anon";
                    RBBINode setRef = usetNode.fParent;
                    if (setRef != null && (varRef = setRef.fParent) != null && varRef.fType == 2) {
                        setName = varRef.fText;
                    }
                    System.out.print(setName);
                    System.out.print(" ");
                }
                i = 0;
                RangeDescriptor tRange = rlRange;
                while (tRange != null) {
                    if (tRange.fNum == rlRange.fNum) {
                        if (i++ % 5 == 0) {
                            System.out.print("\n    ");
                        }
                        RBBINode.printHex(tRange.fStartChar, -1);
                        System.out.print("-");
                        RBBINode.printHex(tRange.fEndChar, 0);
                    }
                    tRange = tRange.fNext;
                }
                System.out.print("\n");
            }
            rlRange = rlRange.fNext;
        }
        System.out.print("\n");
    }

    void printSets() {
        System.out.print("\n\nUnicode Sets List\n------------------\n");
        for (int i = 0; i < this.fRB.fUSetNodes.size(); ++i) {
            RBBINode varRef;
            RBBINode usetNode = this.fRB.fUSetNodes.get(i);
            RBBINode.printInt(2, i);
            String setName = "anonymous";
            RBBINode setRef = usetNode.fParent;
            if (setRef != null && (varRef = setRef.fParent) != null && varRef.fType == 2) {
                setName = varRef.fText;
            }
            System.out.print("  " + setName);
            System.out.print("   ");
            System.out.print(usetNode.fText);
            System.out.print("\n");
            if (usetNode.fLeftChild == null) continue;
            usetNode.fLeftChild.printTree(true);
        }
        System.out.print("\n");
    }

    static class RangeDescriptor {
        int fStartChar = 0;
        int fEndChar = 0;
        int fNum = 0;
        boolean fIncludesDict = false;
        boolean fFirstInGroup = false;
        List<RBBINode> fIncludesSets;
        RangeDescriptor fNext;

        RangeDescriptor() {
            this.fIncludesSets = new ArrayList<RBBINode>();
        }

        RangeDescriptor(RangeDescriptor other) {
            this.fStartChar = other.fStartChar;
            this.fEndChar = other.fEndChar;
            this.fNum = other.fNum;
            this.fIncludesDict = other.fIncludesDict;
            this.fFirstInGroup = other.fFirstInGroup;
            this.fIncludesSets = new ArrayList<RBBINode>(other.fIncludesSets);
        }

        void split(int where) {
            Assert.assrt(where > this.fStartChar && where <= this.fEndChar);
            RangeDescriptor nr = new RangeDescriptor(this);
            nr.fStartChar = where;
            this.fEndChar = where - 1;
            nr.fNext = this.fNext;
            this.fNext = nr;
        }

        boolean isDictionaryRange() {
            for (int i = 0; i < this.fIncludesSets.size(); ++i) {
                RBBINode varRef;
                RBBINode usetNode = this.fIncludesSets.get(i);
                String setName = "";
                RBBINode setRef = usetNode.fParent;
                if (setRef != null && (varRef = setRef.fParent) != null && varRef.fType == 2) {
                    setName = varRef.fText;
                }
                if (!setName.equals("dictionary")) continue;
                return true;
            }
            return false;
        }
    }
}

