/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.hash.IgnoreJRERequirement;
import com.google.common.primitives.Longs;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

final class LittleEndianByteArray {
    private static final LittleEndianBytes byteArray = LittleEndianByteArray.makeGetter();

    static long load64(byte[] input, int offset) {
        assert (input.length >= offset + 8);
        return byteArray.getLongLittleEndian(input, offset);
    }

    static long load64Safely(byte[] input, int offset, int length) {
        long result = 0L;
        int limit = Math.min(length, 8);
        for (int i = 0; i < limit; ++i) {
            result |= ((long)input[offset + i] & 0xFFL) << i * 8;
        }
        return result;
    }

    static void store64(byte[] sink, int offset, long value) {
        assert (offset >= 0 && offset + 8 <= sink.length);
        byteArray.putLongLittleEndian(sink, offset, value);
    }

    static int load32(byte[] source, int offset) {
        return source[offset] & 0xFF | (source[offset + 1] & 0xFF) << 8 | (source[offset + 2] & 0xFF) << 16 | (source[offset + 3] & 0xFF) << 24;
    }

    static boolean usingFastPath() {
        return byteArray.usesFastPath();
    }

    static LittleEndianBytes makeGetter() {
        LittleEndianBytes usingVarHandle = VarHandleLittleEndianBytesMaker.INSTANCE.tryMakeVarHandleLittleEndianBytes();
        if (usingVarHandle != null) {
            return usingVarHandle;
        }
        try {
            String arch = System.getProperty("os.arch");
            if (Objects.equals(arch, "amd64") || Objects.equals(arch, "aarch64")) {
                return ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN) ? UnsafeByteArray.UNSAFE_LITTLE_ENDIAN : UnsafeByteArray.UNSAFE_BIG_ENDIAN;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return JavaLittleEndianBytes.INSTANCE;
    }

    private LittleEndianByteArray() {
    }

    private static interface LittleEndianBytes {
        public long getLongLittleEndian(byte[] var1, int var2);

        public void putLongLittleEndian(byte[] var1, int var2, long var3);

        public boolean usesFastPath();
    }

    private static enum VarHandleLittleEndianBytesMaker {
        INSTANCE{

            @Override
            @Nullable LittleEndianBytes tryMakeVarHandleLittleEndianBytes() {
                try {
                    Class.forName("java.lang.invoke.VarHandle");
                }
                catch (ClassNotFoundException beforeJava9) {
                    return null;
                }
                return VarHandleLittleEndianBytes.INSTANCE;
            }
        };


        @Nullable LittleEndianBytes tryMakeVarHandleLittleEndianBytes() {
            return null;
        }
    }

    @VisibleForTesting
    static enum UnsafeByteArray implements LittleEndianBytes
    {
        UNSAFE_LITTLE_ENDIAN{

            @Override
            public long getLongLittleEndian(byte[] array, int offset) {
                return theUnsafe.getLong(array, (long)offset + (long)BYTE_ARRAY_BASE_OFFSET);
            }

            @Override
            public void putLongLittleEndian(byte[] array, int offset, long value) {
                theUnsafe.putLong(array, (long)offset + (long)BYTE_ARRAY_BASE_OFFSET, value);
            }
        }
        ,
        UNSAFE_BIG_ENDIAN{

            @Override
            public long getLongLittleEndian(byte[] array, int offset) {
                long bigEndian = theUnsafe.getLong(array, (long)offset + (long)BYTE_ARRAY_BASE_OFFSET);
                return Long.reverseBytes(bigEndian);
            }

            @Override
            public void putLongLittleEndian(byte[] array, int offset, long value) {
                long littleEndianValue = Long.reverseBytes(value);
                theUnsafe.putLong(array, (long)offset + (long)BYTE_ARRAY_BASE_OFFSET, littleEndianValue);
            }
        };

        private static final Unsafe theUnsafe;
        private static final int BYTE_ARRAY_BASE_OFFSET;

        @Override
        public boolean usesFastPath() {
            return true;
        }

        private static Unsafe getUnsafe() {
            try {
                return Unsafe.getUnsafe();
            }
            catch (SecurityException securityException) {
                try {
                    return AccessController.doPrivileged(() -> {
                        Class<Unsafe> k = Unsafe.class;
                        for (Field f : k.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (!k.isInstance(x)) continue;
                            return (Unsafe)k.cast(x);
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    });
                }
                catch (PrivilegedActionException e) {
                    throw new RuntimeException("Could not initialize intrinsics", e.getCause());
                }
            }
        }

        static {
            theUnsafe = UnsafeByteArray.getUnsafe();
            BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
            if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
                throw new AssertionError();
            }
        }
    }

    private static enum JavaLittleEndianBytes implements LittleEndianBytes
    {
        INSTANCE{

            @Override
            public long getLongLittleEndian(byte[] source, int offset) {
                return Longs.fromBytes(source[offset + 7], source[offset + 6], source[offset + 5], source[offset + 4], source[offset + 3], source[offset + 2], source[offset + 1], source[offset]);
            }

            @Override
            public void putLongLittleEndian(byte[] sink, int offset, long value) {
                long mask = 255L;
                for (int i = 0; i < 8; ++i) {
                    sink[offset + i] = (byte)((value & mask) >> i * 8);
                    mask <<= 8;
                }
            }

            @Override
            public boolean usesFastPath() {
                return false;
            }
        };

    }

    @IgnoreJRERequirement
    private static enum VarHandleLittleEndianBytes implements LittleEndianBytes
    {
        INSTANCE{

            @Override
            public long getLongLittleEndian(byte[] array, int offset) {
                return HANDLE.get(array, offset);
            }

            @Override
            public void putLongLittleEndian(byte[] array, int offset, long value) {
                HANDLE.set(array, offset, value);
            }
        };

        static final VarHandle HANDLE;

        @Override
        public boolean usesFastPath() {
            return true;
        }

        static {
            HANDLE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
        }
    }
}

