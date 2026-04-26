/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.hash.AbstractNonStreamingHashFunction;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.LittleEndianByteArray;

final class Fingerprint2011
extends AbstractNonStreamingHashFunction {
    static final HashFunction FINGERPRINT_2011 = new Fingerprint2011();
    private static final long K0 = -6505348102511208375L;
    private static final long K1 = -8261664234251669945L;
    private static final long K2 = -4288712594273399085L;
    private static final long K3 = -4132994306676758123L;

    Fingerprint2011() {
    }

    @Override
    public HashCode hashBytes(byte[] input, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, input.length);
        return HashCode.fromLong(Fingerprint2011.fingerprint(input, off, len));
    }

    @Override
    public int bits() {
        return 64;
    }

    public String toString() {
        return "Hashing.fingerprint2011()";
    }

    @VisibleForTesting
    static long fingerprint(byte[] bytes, int offset, int length) {
        long result = length <= 32 ? Fingerprint2011.murmurHash64WithSeed(bytes, offset, length, -1397348546323613475L) : (length <= 64 ? Fingerprint2011.hashLength33To64(bytes, offset, length) : Fingerprint2011.fullFingerprint(bytes, offset, length));
        long u = length >= 8 ? LittleEndianByteArray.load64(bytes, offset) : -6505348102511208375L;
        long v = length >= 9 ? LittleEndianByteArray.load64(bytes, offset + length - 8) : -6505348102511208375L;
        result = Fingerprint2011.hash128to64(result + v, u);
        return result == 0L || result == 1L ? result + -2L : result;
    }

    private static long shiftMix(long val) {
        return val ^ val >>> 47;
    }

    @VisibleForTesting
    static long hash128to64(long high, long low) {
        long a = (low ^ high) * -4132994306676758123L;
        a ^= a >>> 47;
        long b = (high ^ a) * -4132994306676758123L;
        b ^= b >>> 47;
        return b *= -4132994306676758123L;
    }

    private static void weakHashLength32WithSeeds(byte[] bytes, int offset, long seedA, long seedB, long[] output) {
        long part1 = LittleEndianByteArray.load64(bytes, offset);
        long part2 = LittleEndianByteArray.load64(bytes, offset + 8);
        long part3 = LittleEndianByteArray.load64(bytes, offset + 16);
        long part4 = LittleEndianByteArray.load64(bytes, offset + 24);
        seedB = Long.rotateRight(seedB + (seedA += part1) + part4, 51);
        long c = seedA;
        seedA += part2;
        output[0] = seedA + part4;
        output[1] = (seedB += Long.rotateRight(seedA += part3, 23)) + c;
    }

    private static long fullFingerprint(byte[] bytes, int offset, int length) {
        long x = LittleEndianByteArray.load64(bytes, offset);
        long y = LittleEndianByteArray.load64(bytes, offset + length - 16) ^ 0x8D58AC26AFE12E47L;
        long z = LittleEndianByteArray.load64(bytes, offset + length - 56) ^ 0xA5B85C5E198ED849L;
        long[] v = new long[2];
        long[] w = new long[2];
        Fingerprint2011.weakHashLength32WithSeeds(bytes, offset + length - 64, length, y, v);
        Fingerprint2011.weakHashLength32WithSeeds(bytes, offset + length - 32, (long)length * -8261664234251669945L, -6505348102511208375L, w);
        x = Long.rotateRight((z += Fingerprint2011.shiftMix(v[1]) * -8261664234251669945L) + x, 39) * -8261664234251669945L;
        y = Long.rotateRight(y, 33) * -8261664234251669945L;
        length = length - 1 & 0xFFFFFFC0;
        do {
            x = Long.rotateRight(x + y + v[0] + LittleEndianByteArray.load64(bytes, offset + 16), 37) * -8261664234251669945L;
            y = Long.rotateRight(y + v[1] + LittleEndianByteArray.load64(bytes, offset + 48), 42) * -8261664234251669945L;
            z = Long.rotateRight(z ^ w[0], 33);
            Fingerprint2011.weakHashLength32WithSeeds(bytes, offset, v[1] * -8261664234251669945L, (x ^= w[1]) + w[0], v);
            Fingerprint2011.weakHashLength32WithSeeds(bytes, offset + 32, z + w[1], y ^= v[0], w);
            long tmp = z;
            z = x;
            x = tmp;
            offset += 64;
        } while ((length -= 64) != 0);
        return Fingerprint2011.hash128to64(Fingerprint2011.hash128to64(v[0], w[0]) + Fingerprint2011.shiftMix(y) * -8261664234251669945L + z, Fingerprint2011.hash128to64(v[1], w[1]) + x);
    }

    private static long hashLength33To64(byte[] bytes, int offset, int length) {
        long z = LittleEndianByteArray.load64(bytes, offset + 24);
        long a = LittleEndianByteArray.load64(bytes, offset) + ((long)length + LittleEndianByteArray.load64(bytes, offset + length - 16)) * -6505348102511208375L;
        long b = Long.rotateRight(a + z, 52);
        long c = Long.rotateRight(a, 37);
        long vf = (a += LittleEndianByteArray.load64(bytes, offset + 16)) + z;
        long vs = b + Long.rotateRight(a, 31) + (c += Long.rotateRight(a += LittleEndianByteArray.load64(bytes, offset + 8), 7));
        a = LittleEndianByteArray.load64(bytes, offset + 16) + LittleEndianByteArray.load64(bytes, offset + length - 32);
        z = LittleEndianByteArray.load64(bytes, offset + length - 8);
        b = Long.rotateRight(a + z, 52);
        c = Long.rotateRight(a, 37);
        long wf = (a += LittleEndianByteArray.load64(bytes, offset + length - 16)) + z;
        long ws = b + Long.rotateRight(a, 31) + (c += Long.rotateRight(a += LittleEndianByteArray.load64(bytes, offset + length - 24), 7));
        long r = Fingerprint2011.shiftMix((vf + ws) * -4288712594273399085L + (wf + vs) * -6505348102511208375L);
        return Fingerprint2011.shiftMix(r * -6505348102511208375L + vs) * -4288712594273399085L;
    }

    @VisibleForTesting
    static long murmurHash64WithSeed(byte[] bytes, int offset, int length, long seed) {
        long mul = -4132994306676758123L;
        int topBit = 7;
        int lengthAligned = length & ~topBit;
        int lengthRemainder = length & topBit;
        long hash = seed ^ (long)length * mul;
        for (int i = 0; i < lengthAligned; i += 8) {
            long loaded = LittleEndianByteArray.load64(bytes, offset + i);
            long data = Fingerprint2011.shiftMix(loaded * mul) * mul;
            hash ^= data;
            hash *= mul;
        }
        if (lengthRemainder != 0) {
            long data = LittleEndianByteArray.load64Safely(bytes, offset + lengthAligned, lengthRemainder);
            hash ^= data;
            hash *= mul;
        }
        hash = Fingerprint2011.shiftMix(hash) * mul;
        hash = Fingerprint2011.shiftMix(hash);
        return hash;
    }
}

