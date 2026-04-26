/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.breakiter;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.breakiter.DictionaryBreakEngine;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.UResourceBundle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LSTMBreakEngine
extends DictionaryBreakEngine {
    private static final byte MIN_WORD = 2;
    private static final byte MIN_WORD_SPAN = 4;
    private final LSTMData fData;
    private int fScript;
    private final Vectorizer fVectorizer;

    private static float[][] make2DArray(int[] data, int start, int d1, int d2) {
        byte[] bytes = new byte[4];
        float[][] result = new float[d1][d2];
        for (int i = 0; i < d1; ++i) {
            for (int j = 0; j < d2; ++j) {
                int d = data[start++];
                bytes[0] = (byte)(d >> 24);
                bytes[1] = (byte)(d >> 16);
                bytes[2] = (byte)(d >> 8);
                bytes[3] = (byte)d;
                result[i][j] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
            }
        }
        return result;
    }

    private static float[] make1DArray(int[] data, int start, int d1) {
        byte[] bytes = new byte[4];
        float[] result = new float[d1];
        for (int i = 0; i < d1; ++i) {
            int d = data[start++];
            bytes[0] = (byte)(d >> 24);
            bytes[1] = (byte)(d >> 16);
            bytes[2] = (byte)(d >> 8);
            bytes[3] = (byte)d;
            result[i] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
        }
        return result;
    }

    private Vectorizer makeVectorizer(LSTMData data) {
        switch (data.fType) {
            case CODE_POINTS: {
                return new CodePointsVectorizer(data.fDict);
            }
            case GRAPHEME_CLUSTER: {
                return new GraphemeClusterVectorizer(data.fDict);
            }
        }
        return null;
    }

    public LSTMBreakEngine(int script, UnicodeSet set, LSTMData data) {
        this.setCharacters(set);
        this.fScript = script;
        this.fData = data;
        this.fVectorizer = this.makeVectorizer(this.fData);
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean handles(int c) {
        return this.fScript == UCharacter.getIntPropertyValue(c, 4106);
    }

    private static void addDotProductTo(float[] a, float[][] b, float[] result) {
        assert (a.length == b.length);
        assert (b[0].length == result.length);
        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < a.length; ++j) {
                int n = i;
                result[n] = result[n] + a[j] * b[j][i];
            }
        }
    }

    private static void addTo(float[] a, float[] result) {
        assert (a.length == result.length);
        for (int i = 0; i < result.length; ++i) {
            int n = i;
            result[n] = result[n] + a[i];
        }
    }

    private static void hadamardProductTo(float[] a, float[] result) {
        assert (a.length == result.length);
        for (int i = 0; i < result.length; ++i) {
            int n = i;
            result[n] = result[n] * a[i];
        }
    }

    private static void addHadamardProductTo(float[] a, float[] b, float[] result) {
        assert (a.length == result.length);
        assert (b.length == result.length);
        for (int i = 0; i < result.length; ++i) {
            int n = i;
            result[n] = result[n] + a[i] * b[i];
        }
    }

    private static void sigmoid(float[] result, int start, int length) {
        assert (start < result.length);
        assert (start + length <= result.length);
        for (int i = start; i < start + length; ++i) {
            result[i] = (float)(1.0 / (1.0 + Math.exp(-result[i])));
        }
    }

    private static void tanh(float[] result, int start, int length) {
        assert (start < result.length);
        assert (start + length <= result.length);
        for (int i = start; i < start + length; ++i) {
            result[i] = (float)Math.tanh(result[i]);
        }
    }

    private static int maxIndex(float[] data) {
        int index = 0;
        float max = data[0];
        for (int i = 1; i < data.length; ++i) {
            if (!(data[i] > max)) continue;
            max = data[i];
            index = i;
        }
        return index;
    }

    private float[] compute(float[][] W, float[][] U, float[] B, float[] x, float[] h, float[] c) {
        float[] ifco = Arrays.copyOf(B, B.length);
        LSTMBreakEngine.addDotProductTo(x, W, ifco);
        float[] hU = new float[B.length];
        LSTMBreakEngine.addDotProductTo(h, U, ifco);
        int hunits = B.length / 4;
        LSTMBreakEngine.sigmoid(ifco, 0 * hunits, hunits);
        LSTMBreakEngine.sigmoid(ifco, 1 * hunits, hunits);
        LSTMBreakEngine.tanh(ifco, 2 * hunits, hunits);
        LSTMBreakEngine.sigmoid(ifco, 3 * hunits, hunits);
        LSTMBreakEngine.hadamardProductTo(Arrays.copyOfRange(ifco, hunits, 2 * hunits), c);
        LSTMBreakEngine.addHadamardProductTo(Arrays.copyOf(ifco, hunits), Arrays.copyOfRange(ifco, 2 * hunits, 3 * hunits), c);
        h = Arrays.copyOf(c, c.length);
        LSTMBreakEngine.tanh(h, 0, h.length);
        LSTMBreakEngine.hadamardProductTo(Arrays.copyOfRange(ifco, 3 * hunits, 4 * hunits), h);
        return h;
    }

    @Override
    public int divideUpDictionaryRange(CharacterIterator fIter, int rangeStart, int rangeEnd, DictionaryBreakEngine.DequeI foundBreaks, boolean isPhraseBreaking) {
        int beginSize = foundBreaks.size();
        if (rangeEnd - rangeStart < 4) {
            return 0;
        }
        ArrayList<Integer> offsets = new ArrayList<Integer>(rangeEnd - rangeStart);
        ArrayList<Integer> indicies = new ArrayList<Integer>(rangeEnd - rangeStart);
        this.fVectorizer.vectorize(fIter, rangeStart, rangeEnd, offsets, indicies);
        int inputSeqLength = indicies.size();
        int hunits = this.fData.fForwardU.length;
        float[] c = new float[hunits];
        float[][] hBackward = new float[inputSeqLength][hunits];
        for (int i = inputSeqLength - 1; i >= 0; --i) {
            if (i != inputSeqLength - 1) {
                hBackward[i] = Arrays.copyOf(hBackward[i + 1], hunits);
            }
            hBackward[i] = this.compute(this.fData.fBackwardW, this.fData.fBackwardU, this.fData.fBackwardB, this.fData.fEmbedding[(Integer)indicies.get(i)], hBackward[i], c);
        }
        c = new float[hunits];
        float[] forwardH = new float[hunits];
        float[] both = new float[2 * hunits];
        for (int i = 0; i < inputSeqLength; ++i) {
            forwardH = this.compute(this.fData.fForwardW, this.fData.fForwardU, this.fData.fForwardB, this.fData.fEmbedding[(Integer)indicies.get(i)], forwardH, c);
            System.arraycopy(forwardH, 0, both, 0, hunits);
            System.arraycopy(hBackward[i], 0, both, hunits, hunits);
            float[] logp = Arrays.copyOf(this.fData.fOutputB, this.fData.fOutputB.length);
            LSTMBreakEngine.addDotProductTo(both, this.fData.fOutputW, logp);
            int current = LSTMBreakEngine.maxIndex(logp);
            if (current != LSTMClass.BEGIN.ordinal() && current != LSTMClass.SINGLE.ordinal() || i == 0) continue;
            foundBreaks.push((Integer)offsets.get(i));
        }
        return foundBreaks.size() - beginSize;
    }

    public static LSTMData createData(UResourceBundle bundle) {
        return new LSTMData(bundle);
    }

    private static String defaultLSTM(int script) {
        ICUResourceBundle rb = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/brkitr");
        return rb.getStringWithFallback("lstm/" + UScript.getShortName(script));
    }

    public static LSTMData createData(int script) {
        if (script != 23 && script != 24 && script != 28 && script != 38) {
            return null;
        }
        String name = LSTMBreakEngine.defaultLSTM(script);
        name = name.substring(0, name.indexOf("."));
        UResourceBundle rb = UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/brkitr", name, ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        return LSTMBreakEngine.createData(rb);
    }

    public static LSTMBreakEngine create(int script, LSTMData data) {
        String setExpr = "[[:" + UScript.getShortName(script) + ":]&[:LineBreak=SA:]]";
        UnicodeSet set = new UnicodeSet();
        set.applyPattern(setExpr);
        set.compact();
        return new LSTMBreakEngine(script, set, data);
    }

    class GraphemeClusterVectorizer
    extends Vectorizer {
        public GraphemeClusterVectorizer(Map<String, Integer> dict) {
            super(dict);
        }

        private String substring(CharacterIterator text, int startPos, int endPos) {
            int saved = text.getIndex();
            text.setIndex(startPos);
            StringBuilder sb = new StringBuilder();
            char c = text.current();
            while (c != '\uffff' && text.getIndex() < endPos) {
                sb.append(c);
                c = text.next();
            }
            text.setIndex(saved);
            return sb.toString();
        }

        @Override
        public void vectorize(CharacterIterator text, int startPos, int endPos, List<Integer> offsets, List<Integer> indicies) {
            BreakIterator iter = BreakIterator.getCharacterInstance();
            iter.setText(text);
            int last = iter.next(startPos);
            int curr = iter.next();
            while (curr != -1 && curr <= endPos) {
                offsets.add(last);
                String segment = this.substring(text, last, curr);
                int index = this.getIndex(segment);
                indicies.add(index);
                last = curr;
                curr = iter.next();
            }
        }
    }

    class CodePointsVectorizer
    extends Vectorizer {
        public CodePointsVectorizer(Map<String, Integer> dict) {
            super(dict);
        }

        @Override
        public void vectorize(CharacterIterator fIter, int rangeStart, int rangeEnd, List<Integer> offsets, List<Integer> indicies) {
            fIter.setIndex(rangeStart);
            char c = fIter.current();
            while (c != '\uffff' && fIter.getIndex() < rangeEnd) {
                offsets.add(fIter.getIndex());
                indicies.add(this.getIndex(String.valueOf(c)));
                c = fIter.next();
            }
        }
    }

    abstract class Vectorizer {
        private Map<String, Integer> fDict;

        public Vectorizer(Map<String, Integer> dict) {
            this.fDict = dict;
        }

        public abstract void vectorize(CharacterIterator var1, int var2, int var3, List<Integer> var4, List<Integer> var5);

        protected int getIndex(String token) {
            Integer res = this.fDict.get(token);
            return res == null ? this.fDict.size() : res.intValue();
        }
    }

    public static class LSTMData {
        public EmbeddingType fType;
        public String fName;
        public Map<String, Integer> fDict;
        public float[][] fEmbedding;
        public float[][] fForwardW;
        public float[][] fForwardU;
        public float[] fForwardB;
        public float[][] fBackwardW;
        public float[][] fBackwardU;
        public float[] fBackwardB;
        public float[][] fOutputW;
        public float[] fOutputB;

        private LSTMData() {
        }

        public LSTMData(UResourceBundle rb) {
            int embeddings = rb.get("embeddings").getInt();
            int hunits = rb.get("hunits").getInt();
            this.fType = EmbeddingType.UNKNOWN;
            this.fName = rb.get("model").getString();
            String typeString = rb.get("type").getString();
            if (typeString.equals("codepoints")) {
                this.fType = EmbeddingType.CODE_POINTS;
            } else if (typeString.equals("graphclust")) {
                this.fType = EmbeddingType.GRAPHEME_CLUSTER;
            }
            String[] dict = rb.get("dict").getStringArray();
            int[] data = rb.get("data").getIntVector();
            int dataLen = data.length;
            int numIndex = dict.length;
            this.fDict = new HashMap<String, Integer>(numIndex + 1);
            int idx = 0;
            for (String embedding : dict) {
                this.fDict.put(embedding, idx++);
            }
            int mat1Size = (numIndex + 1) * embeddings;
            int mat2Size = embeddings * 4 * hunits;
            int mat3Size = hunits * 4 * hunits;
            int mat4Size = 4 * hunits;
            int mat5Size = mat2Size;
            int mat6Size = mat3Size;
            int mat7Size = mat4Size;
            int mat8Size = 2 * hunits * 4;
            int mat9Size = 4;
            assert (dataLen == mat1Size + mat2Size + mat3Size + mat4Size + mat5Size + mat6Size + mat7Size + mat8Size + mat9Size);
            int start = 0;
            this.fEmbedding = LSTMBreakEngine.make2DArray(data, start, numIndex + 1, embeddings);
            this.fForwardW = LSTMBreakEngine.make2DArray(data, start += mat1Size, embeddings, 4 * hunits);
            this.fForwardU = LSTMBreakEngine.make2DArray(data, start += mat2Size, hunits, 4 * hunits);
            this.fForwardB = LSTMBreakEngine.make1DArray(data, start += mat3Size, 4 * hunits);
            this.fBackwardW = LSTMBreakEngine.make2DArray(data, start += mat4Size, embeddings, 4 * hunits);
            this.fBackwardU = LSTMBreakEngine.make2DArray(data, start += mat5Size, hunits, 4 * hunits);
            this.fBackwardB = LSTMBreakEngine.make1DArray(data, start += mat6Size, 4 * hunits);
            this.fOutputW = LSTMBreakEngine.make2DArray(data, start += mat7Size, 2 * hunits, 4);
            this.fOutputB = LSTMBreakEngine.make1DArray(data, start += mat8Size, 4);
        }
    }

    public static enum LSTMClass {
        BEGIN,
        INSIDE,
        END,
        SINGLE;

    }

    public static enum EmbeddingType {
        UNKNOWN,
        CODE_POINTS,
        GRAPHEME_CLUSTER;

    }
}

