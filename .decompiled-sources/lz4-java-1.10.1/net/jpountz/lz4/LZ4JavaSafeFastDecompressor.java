/*
 * Decompiled with CFR 0.152.
 */
package net.jpountz.lz4;

import java.nio.ByteBuffer;
import net.jpountz.lz4.LZ4ByteBufferUtils;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeUtils;
import net.jpountz.lz4.LZ4Utils;
import net.jpountz.util.ByteBufferUtils;
import net.jpountz.util.SafeUtils;

final class LZ4JavaSafeFastDecompressor
extends LZ4FastDecompressor {
    public static final LZ4FastDecompressor INSTANCE = new LZ4JavaSafeFastDecompressor();

    LZ4JavaSafeFastDecompressor() {
    }

    @Override
    public int decompress(byte[] src, int srcOff, byte[] dest, int destOff, int destLen) {
        int srcEnd = src.length;
        return this.decompress(src, srcOff, srcEnd - srcOff, dest, destOff, destLen);
    }

    private int decompress(byte[] src, int srcOff, int srcLen, byte[] dest, int destOff, int destLen) {
        int literalCopyEnd;
        int literalLen;
        SafeUtils.checkRange(src, srcOff, srcLen);
        SafeUtils.checkRange(dest, destOff, destLen);
        if (destLen == 0) {
            if (srcLen < 1 || SafeUtils.readByte(src, srcOff) != 0) {
                throw new LZ4Exception("Malformed input at " + srcOff);
            }
            return 1;
        }
        int srcEnd = srcOff + srcLen;
        int destEnd = destOff + destLen;
        int sOff = srcOff;
        int dOff = destOff;
        while (true) {
            int matchCopyEnd;
            if (sOff >= srcEnd) {
                throw new LZ4Exception("Malformed input at " + sOff);
            }
            int token = SafeUtils.readByte(src, sOff) & 0xFF;
            ++sOff;
            literalLen = token >>> 4;
            if (literalLen == 15) {
                int len = -1;
                while (sOff < srcEnd) {
                    byte by = SafeUtils.readByte(src, sOff++);
                    len = by;
                    if (by != -1) break;
                    if ((literalLen += 255) >= 0) continue;
                    throw new LZ4Exception("Too large literalLen");
                }
                literalLen += len & 0xFF;
            }
            if ((literalCopyEnd = dOff + literalLen) < dOff) {
                throw new LZ4Exception("Too large literalLen");
            }
            if (LZ4Utils.notEnoughSpace(destEnd - literalCopyEnd, 8) || LZ4Utils.notEnoughSpace(srcEnd - sOff, 8 + literalLen)) {
                if (literalCopyEnd != destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                if (LZ4Utils.notEnoughSpace(srcEnd - sOff, literalLen)) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                break;
            }
            LZ4SafeUtils.wildArraycopy(src, sOff, dest, dOff, literalLen);
            dOff = literalCopyEnd;
            int matchDec = SafeUtils.readShortLE(src, sOff += literalLen);
            sOff += 2;
            int matchOff = dOff - matchDec;
            if (matchOff < destOff) {
                throw new LZ4Exception("Malformed input at " + sOff);
            }
            int matchLen = token & 0xF;
            if (matchLen == 15) {
                int len = -1;
                while (sOff < srcEnd) {
                    byte by = SafeUtils.readByte(src, sOff++);
                    len = by;
                    if (by != -1) break;
                    if ((matchLen += 255) >= 0) continue;
                    throw new LZ4Exception("Too large matchLen");
                }
                matchLen += len & 0xFF;
            }
            if ((matchCopyEnd = dOff + (matchLen += 4)) < dOff) {
                throw new LZ4Exception("Too large matchLen");
            }
            if (matchDec == 0) {
                if (matchCopyEnd > destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                assert (matchOff == dOff);
                LZ4Utils.zero(dest, dOff, matchCopyEnd);
            } else if (LZ4Utils.notEnoughSpace(destEnd - matchCopyEnd, 8)) {
                if (matchCopyEnd > destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                LZ4SafeUtils.safeIncrementalCopy(dest, matchOff, dOff, matchLen);
            } else {
                LZ4SafeUtils.wildIncrementalCopy(dest, matchOff, dOff, matchCopyEnd);
            }
            dOff = matchCopyEnd;
        }
        LZ4SafeUtils.safeArraycopy(src, sOff, dest, dOff, literalLen);
        dOff = literalCopyEnd;
        return (sOff += literalLen) - srcOff;
    }

    @Override
    public int decompress(ByteBuffer src, int srcOff, ByteBuffer dest, int destOff, int destLen) {
        int srcEnd = src.capacity();
        return this.decompress(src, srcOff, srcEnd - srcOff, dest, destOff, destLen);
    }

    private int decompress(ByteBuffer src, int srcOff, int srcLen, ByteBuffer dest, int destOff, int destLen) {
        int literalCopyEnd;
        int literalLen;
        ByteBufferUtils.checkRange(src, srcOff, srcLen);
        ByteBufferUtils.checkRange(dest, destOff, destLen);
        if (src.hasArray() && dest.hasArray()) {
            return this.decompress(src.array(), srcOff + src.arrayOffset(), srcLen, dest.array(), destOff + dest.arrayOffset(), destLen);
        }
        src = ByteBufferUtils.inNativeByteOrder(src);
        dest = ByteBufferUtils.inNativeByteOrder(dest);
        if (destLen == 0) {
            if (srcLen < 1 || ByteBufferUtils.readByte(src, srcOff) != 0) {
                throw new LZ4Exception("Malformed input at " + srcOff);
            }
            return 1;
        }
        int srcEnd = srcOff + srcLen;
        int destEnd = destOff + destLen;
        int sOff = srcOff;
        int dOff = destOff;
        while (true) {
            int matchCopyEnd;
            if (sOff >= srcEnd) {
                throw new LZ4Exception("Malformed input at " + sOff);
            }
            int token = ByteBufferUtils.readByte(src, sOff) & 0xFF;
            ++sOff;
            literalLen = token >>> 4;
            if (literalLen == 15) {
                int len = -1;
                while (sOff < srcEnd) {
                    byte by = ByteBufferUtils.readByte(src, sOff++);
                    len = by;
                    if (by != -1) break;
                    if ((literalLen += 255) >= 0) continue;
                    throw new LZ4Exception("Too large literalLen");
                }
                literalLen += len & 0xFF;
            }
            if ((literalCopyEnd = dOff + literalLen) < dOff) {
                throw new LZ4Exception("Too large literalLen");
            }
            if (LZ4Utils.notEnoughSpace(destEnd - literalCopyEnd, 8) || LZ4Utils.notEnoughSpace(srcEnd - sOff, 8 + literalLen)) {
                if (literalCopyEnd != destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                if (LZ4Utils.notEnoughSpace(srcEnd - sOff, literalLen)) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                break;
            }
            LZ4ByteBufferUtils.wildArraycopy(src, sOff, dest, dOff, literalLen);
            dOff = literalCopyEnd;
            int matchDec = ByteBufferUtils.readShortLE(src, sOff += literalLen);
            sOff += 2;
            int matchOff = dOff - matchDec;
            if (matchOff < destOff) {
                throw new LZ4Exception("Malformed input at " + sOff);
            }
            int matchLen = token & 0xF;
            if (matchLen == 15) {
                int len = -1;
                while (sOff < srcEnd) {
                    byte by = ByteBufferUtils.readByte(src, sOff++);
                    len = by;
                    if (by != -1) break;
                    if ((matchLen += 255) >= 0) continue;
                    throw new LZ4Exception("Too large matchLen");
                }
                matchLen += len & 0xFF;
            }
            if ((matchCopyEnd = dOff + (matchLen += 4)) < dOff) {
                throw new LZ4Exception("Too large matchLen");
            }
            if (matchDec == 0) {
                if (matchCopyEnd > destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                assert (matchOff == dOff);
                LZ4Utils.zero(dest, dOff, matchCopyEnd);
            } else if (LZ4Utils.notEnoughSpace(destEnd - matchCopyEnd, 8)) {
                if (matchCopyEnd > destEnd) {
                    throw new LZ4Exception("Malformed input at " + sOff);
                }
                LZ4ByteBufferUtils.safeIncrementalCopy(dest, matchOff, dOff, matchLen);
            } else {
                LZ4ByteBufferUtils.wildIncrementalCopy(dest, matchOff, dOff, matchCopyEnd);
            }
            dOff = matchCopyEnd;
        }
        LZ4ByteBufferUtils.safeArraycopy(src, sOff, dest, dOff, literalLen);
        dOff = literalCopyEnd;
        return (sOff += literalLen) - srcOff;
    }
}

