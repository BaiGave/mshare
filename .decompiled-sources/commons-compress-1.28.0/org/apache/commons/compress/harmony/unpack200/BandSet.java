/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.compress.harmony.pack200.BHSDCodec;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CodecEncoding;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PopulationCodec;
import org.apache.commons.compress.harmony.unpack200.CpBands;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.SegmentHeader;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPDouble;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPFieldRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPFloat;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPInteger;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPInterfaceMethodRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPLong;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMethodRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPString;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.utils.ExactMath;
import org.apache.commons.lang3.ArrayUtils;

public abstract class BandSet {
    protected Segment segment;
    protected SegmentHeader header;

    public BandSet(Segment segment) {
        this.segment = segment;
        this.header = segment.getSegmentHeader();
    }

    public int[] decodeBandInt(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] band;
        if (count < 0) {
            throw new Pack200Exception("count < 0");
        }
        Codec codecUsed = codec;
        if (codec.getB() == 1 || count == 0) {
            return codec.decodeInts(count, in);
        }
        int[] getFirst = codec.decodeInts(1, in);
        if (getFirst.length == 0) {
            return getFirst;
        }
        int first = getFirst[0];
        if (codec.isSigned() && first >= -256 && first <= -1) {
            codecUsed = CodecEncoding.getCodec(-1 - first, this.header.getBandHeadersInputStream(), codec);
            band = codecUsed.decodeInts(count, in);
        } else if (!codec.isSigned() && first >= codec.getL() && first <= codec.getL() + 255) {
            codecUsed = CodecEncoding.getCodec(first - codec.getL(), this.header.getBandHeadersInputStream(), codec);
            band = codecUsed.decodeInts(count, in);
        } else {
            band = codec.decodeInts(count - 1, in, first);
        }
        if (codecUsed instanceof PopulationCodec) {
            PopulationCodec popCodec = (PopulationCodec)codecUsed;
            int[] favoured = (int[])popCodec.getFavoured().clone();
            Arrays.sort(favoured);
            for (int i = 0; i < band.length; ++i) {
                Codec theCodec;
                boolean favouredValue = Arrays.binarySearch(favoured, band[i]) > -1;
                Codec codec2 = theCodec = favouredValue ? popCodec.getFavouredCodec() : popCodec.getUnfavouredCodec();
                if (!(theCodec instanceof BHSDCodec) || !((BHSDCodec)theCodec).isDelta()) continue;
                BHSDCodec bhsd = (BHSDCodec)theCodec;
                long cardinality = bhsd.cardinality();
                while ((long)band[i] > bhsd.largest()) {
                    int n = i;
                    band[n] = (int)((long)band[n] - cardinality);
                }
                while ((long)band[i] < bhsd.smallest()) {
                    band[i] = ExactMath.add(band[i], cardinality);
                }
            }
        }
        return band;
    }

    public int[][] decodeBandInt(String name, InputStream in, BHSDCodec defaultCodec, int[] counts) throws IOException, Pack200Exception {
        int[][] result = new int[counts.length][];
        int totalCount = 0;
        for (int count : counts) {
            totalCount += count;
        }
        int[] twoDResult = this.decodeBandInt(name, in, defaultCodec, totalCount);
        int index = 0;
        for (int i = 0; i < result.length; ++i) {
            if (counts[i] > twoDResult.length) {
                throw new IOException("Counts value exceeds length of twoDResult");
            }
            result[i] = new int[counts[i]];
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = twoDResult[index];
                ++index;
            }
        }
        return result;
    }

    protected String[] getReferences(int[] ints, String[] reference) {
        return ArrayUtils.setAll(new String[ints.length], i -> reference[ints[i]]);
    }

    protected String[][] getReferences(int[][] ints, String[] reference) {
        String[][] result = new String[ints.length][];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new String[ints[i].length];
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = reference[ints[i][j]];
            }
        }
        return result;
    }

    public CPClass[] parseCPClassReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPClass[indices.length], i -> cpBands.cpClassValue(indices[i]));
    }

    public CPNameAndType[] parseCPDescriptorReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPNameAndType[indices.length], i -> cpBands.cpNameAndTypeValue(indices[i]));
    }

    public CPDouble[] parseCPDoubleReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPDouble[indices.length], i -> cpBands.cpDoubleValue(indices[i]));
    }

    public CPFieldRef[] parseCPFieldRefReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPFieldRef[indices.length], i -> cpBands.cpFieldValue(indices[i]));
    }

    public CPFloat[] parseCPFloatReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPFloat[indices.length], i -> cpBands.cpFloatValue(indices[i]));
    }

    public CPInterfaceMethodRef[] parseCPInterfaceMethodRefReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPInterfaceMethodRef[indices.length], i -> cpBands.cpIMethodValue(indices[i]));
    }

    public CPInteger[] parseCPIntReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        CpBands cpBands = this.segment.getCpBands();
        int[] reference = cpBands.getCpInt();
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CPInteger[] result = new CPInteger[indices.length];
        for (int i = 0; i < count; ++i) {
            int index = indices[i];
            if (index < 0 || index >= reference.length) {
                throw new Pack200Exception("Something has gone wrong during parsing references, index = " + index + ", array size = " + reference.length);
            }
            result[i] = cpBands.cpIntegerValue(index);
        }
        return result;
    }

    public CPLong[] parseCPLongReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        CpBands cpBands = this.segment.getCpBands();
        long[] reference = cpBands.getCpLong();
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CPLong[] result = new CPLong[indices.length];
        for (int i = 0; i < count; ++i) {
            int index = indices[i];
            if (index < 0 || index >= reference.length) {
                throw new Pack200Exception("Something has gone wrong during parsing references, index = " + index + ", array size = " + reference.length);
            }
            result[i] = cpBands.cpLongValue(index);
        }
        return result;
    }

    public CPMethodRef[] parseCPMethodRefReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPMethodRef[indices.length], i -> cpBands.cpMethodValue(indices[i]));
    }

    public CPUTF8[] parseCPSignatureReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPUTF8[indices.length], i -> cpBands.cpSignatureValue(indices[i]));
    }

    protected CPUTF8[][] parseCPSignatureReferences(String name, InputStream in, BHSDCodec codec, int[] counts) throws IOException, Pack200Exception {
        int sum = 0;
        for (int count : counts) {
            sum += count;
        }
        int[] indices = this.decodeBandInt(name, in, codec, sum);
        CpBands cpBands = this.segment.getCpBands();
        CPUTF8[] result1 = ArrayUtils.setAll(new CPUTF8[sum], i -> cpBands.cpSignatureValue(indices[i]));
        int pos = 0;
        CPUTF8[][] result = new CPUTF8[counts.length][];
        for (int i2 = 0; i2 < counts.length; ++i2) {
            int num = counts[i2];
            result[i2] = new CPUTF8[num];
            System.arraycopy(result1, pos, result[i2], 0, num);
            pos += num;
        }
        return result;
    }

    public CPString[] parseCPStringReferences(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPString[indices.length], i -> cpBands.cpStringValue(indices[i]));
    }

    public CPUTF8[] parseCPUTF8References(String name, InputStream in, BHSDCodec codec, int count) throws IOException, Pack200Exception {
        int[] indices = this.decodeBandInt(name, in, codec, count);
        CpBands cpBands = this.segment.getCpBands();
        return ArrayUtils.setAll(new CPUTF8[indices.length], i -> cpBands.cpUTF8Value(indices[i]));
    }

    public CPUTF8[][] parseCPUTF8References(String name, InputStream in, BHSDCodec codec, int[] counts) throws IOException, Pack200Exception {
        CPUTF8[][] result = new CPUTF8[counts.length][];
        int sum = 0;
        for (int i2 = 0; i2 < counts.length; ++i2) {
            result[i2] = new CPUTF8[counts[i2]];
            sum += counts[i2];
        }
        int[] indices = this.decodeBandInt(name, in, codec, sum);
        CpBands cpBands = this.segment.getCpBands();
        CPUTF8[] result1 = ArrayUtils.setAll(new CPUTF8[sum], i -> cpBands.cpUTF8Value(indices[i]));
        int pos = 0;
        for (int i3 = 0; i3 < counts.length; ++i3) {
            int num = counts[i3];
            result[i3] = new CPUTF8[num];
            System.arraycopy(result1, pos, result[i3], 0, num);
            pos += num;
        }
        return result;
    }

    public long[] parseFlags(String name, InputStream in, int count, BHSDCodec hiCodec, BHSDCodec loCodec) throws IOException, Pack200Exception {
        return this.parseFlags(name, in, new int[]{count}, hiCodec, loCodec)[0];
    }

    public long[] parseFlags(String name, InputStream in, int count, BHSDCodec codec, boolean hasHi) throws IOException, Pack200Exception {
        return this.parseFlags(name, in, new int[]{count}, hasHi ? codec : null, codec)[0];
    }

    public long[][] parseFlags(String name, InputStream in, int[] counts, BHSDCodec hiCodec, BHSDCodec loCodec) throws IOException, Pack200Exception {
        int count = counts.length;
        if (count == 0) {
            return new long[][]{new long[0]};
        }
        int sum = 0;
        long[][] result = new long[count][];
        for (int i = 0; i < count; ++i) {
            sum += counts[i];
        }
        int[] hi = null;
        if (hiCodec != null) {
            hi = this.decodeBandInt(name, in, hiCodec, sum);
        }
        int[] lo = this.decodeBandInt(name, in, loCodec, sum);
        int index = 0;
        for (int i = 0; i < count; ++i) {
            result[i] = new long[counts[i]];
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = hi != null ? (long)hi[index] << 32 | (long)lo[index] & 0xFFFFFFFFL : (long)lo[index];
                ++index;
            }
        }
        return result;
    }

    public long[][] parseFlags(String name, InputStream in, int[] counts, BHSDCodec codec, boolean hasHi) throws IOException, Pack200Exception {
        return this.parseFlags(name, in, counts, hasHi ? codec : null, codec);
    }

    public String[] parseReferences(String name, InputStream in, BHSDCodec codec, int count, String[] reference) throws IOException, Pack200Exception {
        return this.parseReferences(name, in, codec, new int[]{count}, reference)[0];
    }

    public String[][] parseReferences(String name, InputStream in, BHSDCodec codec, int[] counts, String[] reference) throws IOException, Pack200Exception {
        int count = counts.length;
        if (count == 0) {
            return new String[][]{new String[0]};
        }
        int sum = 0;
        for (int i = 0; i < count; ++i) {
            sum += counts[i];
        }
        int[] indices = this.decodeBandInt(name, in, codec, sum);
        String[] result1 = new String[sum];
        for (int i1 = 0; i1 < sum; ++i1) {
            int index = indices[i1];
            if (index < 0 || index >= reference.length) {
                throw new Pack200Exception("Something has gone wrong during parsing references, index = " + index + ", array size = " + reference.length);
            }
            result1[i1] = reference[index];
        }
        String[][] result = new String[count][];
        int pos = 0;
        for (int i = 0; i < count; ++i) {
            int num = counts[i];
            result[i] = new String[num];
            System.arraycopy(result1, pos, result[i], 0, num);
            pos += num;
        }
        return result;
    }

    public abstract void read(InputStream var1) throws IOException, Pack200Exception;

    public abstract void unpack() throws IOException, Pack200Exception;

    public void unpack(InputStream in) throws IOException, Pack200Exception {
        this.read(in);
        this.unpack();
    }
}

