/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.UncheckedException;

public class RandomUtils {
    private static RandomUtils INSECURE = new RandomUtils(ThreadLocalRandom::current);
    private static RandomUtils SECURE = new RandomUtils(SecureRandom::new);
    private static final Supplier<Random> SECURE_STRONG_SUPPLIER = () -> SECURE_RANDOM_STRONG.get();
    private static RandomUtils SECURE_STRONG = new RandomUtils(SECURE_STRONG_SUPPLIER);
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_STRONG = ThreadLocal.withInitial(() -> {
        try {
            return SecureRandom.getInstanceStrong();
        }
        catch (NoSuchAlgorithmException e) {
            throw new UncheckedException(e);
        }
    });
    private final Supplier<Random> random;

    public static RandomUtils insecure() {
        return INSECURE;
    }

    @Deprecated
    public static boolean nextBoolean() {
        return RandomUtils.secure().randomBoolean();
    }

    @Deprecated
    public static byte[] nextBytes(int count) {
        return RandomUtils.secure().randomBytes(count);
    }

    @Deprecated
    public static double nextDouble() {
        return RandomUtils.secure().randomDouble();
    }

    @Deprecated
    public static double nextDouble(double startInclusive, double endExclusive) {
        return RandomUtils.secure().randomDouble(startInclusive, endExclusive);
    }

    @Deprecated
    public static float nextFloat() {
        return RandomUtils.secure().randomFloat();
    }

    @Deprecated
    public static float nextFloat(float startInclusive, float endExclusive) {
        return RandomUtils.secure().randomFloat(startInclusive, endExclusive);
    }

    @Deprecated
    public static int nextInt() {
        return RandomUtils.secure().randomInt();
    }

    @Deprecated
    public static int nextInt(int startInclusive, int endExclusive) {
        return RandomUtils.secure().randomInt(startInclusive, endExclusive);
    }

    @Deprecated
    public static long nextLong() {
        return RandomUtils.secure().randomLong();
    }

    @Deprecated
    public static long nextLong(long startInclusive, long endExclusive) {
        return RandomUtils.secure().randomLong(startInclusive, endExclusive);
    }

    public static RandomUtils secure() {
        return SECURE;
    }

    static SecureRandom secureRandom() {
        return SECURE_RANDOM_STRONG.get();
    }

    public static RandomUtils secureStrong() {
        return SECURE_STRONG;
    }

    @Deprecated
    public RandomUtils() {
        this(SECURE_STRONG_SUPPLIER);
    }

    private RandomUtils(Supplier<Random> random) {
        this.random = random;
    }

    Random random() {
        return this.random.get();
    }

    public boolean randomBoolean() {
        return this.random().nextBoolean();
    }

    public byte[] randomBytes(int count) {
        Validate.isTrue(count >= 0, "Count cannot be negative.", new Object[0]);
        byte[] result = new byte[count];
        this.random().nextBytes(result);
        return result;
    }

    public double randomDouble() {
        return this.randomDouble(0.0, Double.MAX_VALUE);
    }

    public double randomDouble(double startInclusive, double endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(startInclusive >= 0.0, "Both range values must be non-negative.", new Object[0]);
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + (endExclusive - startInclusive) * this.random().nextDouble();
    }

    public float randomFloat() {
        return this.randomFloat(0.0f, Float.MAX_VALUE);
    }

    public float randomFloat(float startInclusive, float endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(startInclusive >= 0.0f, "Both range values must be non-negative.", new Object[0]);
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + (endExclusive - startInclusive) * this.random().nextFloat();
    }

    public int randomInt() {
        return this.randomInt(0, Integer.MAX_VALUE);
    }

    public int randomInt(int startInclusive, int endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.", new Object[0]);
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + this.random().nextInt(endExclusive - startInclusive);
    }

    public long randomLong() {
        return this.randomLong(Long.MAX_VALUE);
    }

    private long randomLong(long n) {
        long val;
        long bits;
        while ((bits = this.random().nextLong() >>> 1) - (val = bits % n) + n - 1L < 0L) {
        }
        return val;
    }

    public long randomLong(long startInclusive, long endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(startInclusive >= 0L, "Both range values must be non-negative.", new Object[0]);
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + this.randomLong(endExclusive - startInclusive);
    }

    public String toString() {
        return "RandomUtils [random=" + this.random() + "]";
    }
}

