/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.Mth;

public class SpatialLongSet
extends LongLinkedOpenHashSet {
    private final InternalMap map;

    public SpatialLongSet(int expected, float f) {
        super(expected, f);
        this.map = new InternalMap(expected / 64, f);
    }

    @Override
    public boolean add(long k) {
        return this.map.addBit(k);
    }

    @Override
    public boolean rem(long k) {
        return this.map.removeBit(k);
    }

    @Override
    public long removeFirstLong() {
        return this.map.removeFirstBit();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    protected static class InternalMap
    extends Long2LongLinkedOpenHashMap {
        private static final int X_BITS = Mth.log2(60000000);
        private static final int Z_BITS = Mth.log2(60000000);
        private static final int Y_BITS;
        private static final int Y_OFFSET = 0;
        private static final int Z_OFFSET;
        private static final int X_OFFSET;
        private static final long OUTER_MASK;
        private int lastPos = -1;
        private long lastOuterKey;
        private final int minSize;

        public InternalMap(int expected, float f) {
            super(expected, f);
            this.minSize = expected;
        }

        static long getOuterKey(long key) {
            return key & (OUTER_MASK ^ 0xFFFFFFFFFFFFFFFFL);
        }

        static int getInnerKey(long key) {
            int innerX = (int)(key >>> X_OFFSET & 3L);
            int innerY = (int)(key >>> 0 & 3L);
            int innerZ = (int)(key >>> Z_OFFSET & 3L);
            return innerX << 4 | innerZ << 2 | innerY;
        }

        static long getFullKey(long outerKey, int innerKey) {
            outerKey |= (long)(innerKey >>> 4 & 3) << X_OFFSET;
            outerKey |= (long)(innerKey >>> 2 & 3) << Z_OFFSET;
            return outerKey |= (long)(innerKey >>> 0 & 3) << 0;
        }

        public boolean addBit(long key) {
            int pos;
            long outerKey = InternalMap.getOuterKey(key);
            int innerKey = InternalMap.getInnerKey(key);
            long bitMask = 1L << innerKey;
            if (outerKey == 0L) {
                if (this.containsNullKey) {
                    return this.replaceBit(this.n, bitMask);
                }
                this.containsNullKey = true;
                pos = this.n;
            } else {
                if (this.lastPos != -1 && outerKey == this.lastOuterKey) {
                    return this.replaceBit(this.lastPos, bitMask);
                }
                long[] keys = this.key;
                pos = (int)HashCommon.mix(outerKey) & this.mask;
                long curr = keys[pos];
                while (curr != 0L) {
                    if (curr == outerKey) {
                        this.lastPos = pos;
                        this.lastOuterKey = outerKey;
                        return this.replaceBit(pos, bitMask);
                    }
                    pos = pos + 1 & this.mask;
                    curr = keys[pos];
                }
            }
            this.key[pos] = outerKey;
            this.value[pos] = bitMask;
            if (this.size == 0) {
                this.first = this.last = pos;
                this.link[pos] = -1L;
            } else {
                int n = this.last;
                this.link[n] = this.link[n] ^ (this.link[this.last] ^ (long)pos & 0xFFFFFFFFL) & 0xFFFFFFFFL;
                this.link[pos] = ((long)this.last & 0xFFFFFFFFL) << 32 | 0xFFFFFFFFL;
                this.last = pos;
            }
            if (this.size++ >= this.maxFill) {
                this.rehash(HashCommon.arraySize(this.size + 1, this.f));
            }
            return false;
        }

        private boolean replaceBit(int pos, long bitMask) {
            boolean oldValue = (this.value[pos] & bitMask) != 0L;
            int n = pos;
            this.value[n] = this.value[n] | bitMask;
            return oldValue;
        }

        public boolean removeBit(long key) {
            long outerKey = InternalMap.getOuterKey(key);
            int innerKey = InternalMap.getInnerKey(key);
            long bitMask = 1L << innerKey;
            if (outerKey == 0L) {
                if (this.containsNullKey) {
                    return this.removeFromNullEntry(bitMask);
                }
                return false;
            }
            if (this.lastPos != -1 && outerKey == this.lastOuterKey) {
                return this.removeFromEntry(this.lastPos, bitMask);
            }
            long[] keys = this.key;
            int pos = (int)HashCommon.mix(outerKey) & this.mask;
            long curr = keys[pos];
            while (curr != 0L) {
                if (outerKey == curr) {
                    this.lastPos = pos;
                    this.lastOuterKey = outerKey;
                    return this.removeFromEntry(pos, bitMask);
                }
                pos = pos + 1 & this.mask;
                curr = keys[pos];
            }
            return false;
        }

        private boolean removeFromNullEntry(long bitMask) {
            if ((this.value[this.n] & bitMask) == 0L) {
                return false;
            }
            int n = this.n;
            this.value[n] = this.value[n] & (bitMask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[this.n] != 0L) {
                return true;
            }
            this.containsNullKey = false;
            --this.size;
            this.fixPointers(this.n);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        private boolean removeFromEntry(int pos, long bitMask) {
            if ((this.value[pos] & bitMask) == 0L) {
                return false;
            }
            int n = pos;
            this.value[n] = this.value[n] & (bitMask ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[pos] != 0L) {
                return true;
            }
            this.lastPos = -1;
            --this.size;
            this.fixPointers(pos);
            this.shiftKeys(pos);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        public long removeFirstBit() {
            if (this.size == 0) {
                throw new NoSuchElementException();
            }
            int pos = this.first;
            long outerKey = this.key[pos];
            int innerKey = Long.numberOfTrailingZeros(this.value[pos]);
            int n = pos;
            this.value[n] = this.value[n] & (1L << innerKey ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[pos] == 0L) {
                this.removeFirstLong();
                this.lastPos = -1;
            }
            return InternalMap.getFullKey(outerKey, innerKey);
        }

        @Override
        protected void rehash(int newN) {
            if (newN > this.minSize) {
                super.rehash(newN);
            }
        }

        static {
            Z_OFFSET = Y_BITS = 64 - X_BITS - Z_BITS;
            X_OFFSET = Y_BITS + Z_BITS;
            OUTER_MASK = 3L << X_OFFSET | 3L | 3L << Z_OFFSET;
        }
    }
}

