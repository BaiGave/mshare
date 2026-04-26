/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.Random;
import java.util.function.Supplier;
import org.apache.commons.lang3.CachedRandomBits;
import org.apache.commons.lang3.RandomUtils;

public class RandomStringUtils {
    private static final Supplier<RandomUtils> SECURE_SUPPLIER = RandomUtils::secure;
    private static RandomStringUtils INSECURE = new RandomStringUtils(RandomUtils::insecure);
    private static RandomStringUtils SECURE = new RandomStringUtils(SECURE_SUPPLIER);
    private static RandomStringUtils SECURE_STRONG = new RandomStringUtils(RandomUtils::secureStrong);
    private static final char[] ALPHANUMERICAL_CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int ASCII_0 = 48;
    private static final int ASCII_9 = 57;
    private static final int ASCII_A = 65;
    private static final int ASCII_z = 122;
    private static final int CACHE_PADDING_BITS = 3;
    private static final int BITS_TO_BYTES_DIVISOR = 5;
    private static final int BASE_CACHE_SIZE_PADDING = 10;
    private final Supplier<RandomUtils> random;

    public static RandomStringUtils insecure() {
        return INSECURE;
    }

    @Deprecated
    public static String random(int count) {
        return RandomStringUtils.secure().next(count);
    }

    @Deprecated
    public static String random(int count, boolean letters, boolean numbers) {
        return RandomStringUtils.secure().next(count, letters, numbers);
    }

    @Deprecated
    public static String random(int count, char ... chars) {
        return RandomStringUtils.secure().next(count, chars);
    }

    @Deprecated
    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return RandomStringUtils.secure().next(count, start, end, letters, numbers);
    }

    @Deprecated
    public static String random(int count, int start, int end, boolean letters, boolean numbers, char ... chars) {
        return RandomStringUtils.secure().next(count, start, end, letters, numbers, chars);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random) {
        if (count == 0) {
            return "";
        }
        if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }
        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else if (!letters && !numbers) {
                end = 0x10FFFF;
            } else {
                end = 123;
                start = 32;
            }
        } else {
            if (end <= start) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
            }
            if (start < 0 || end < 0) {
                throw new IllegalArgumentException("Character positions MUST be >= 0");
            }
        }
        if (end > 0x10FFFF) {
            end = 0x10FFFF;
        }
        if (chars == null && end <= 127) {
            if (letters && numbers && start <= 48 && end >= 123) {
                return RandomStringUtils.random(count, 0, 0, false, false, ALPHANUMERICAL_CHARS, random);
            }
            if (numbers && end <= 48 || letters && end <= 65) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater then (" + 48 + ") for generating digits or greater then (" + 65 + ") for generating letters.");
            }
            if (letters && numbers) {
                start = Math.max(48, start);
                end = Math.min(123, end);
            } else if (numbers) {
                start = Math.max(48, start);
                end = Math.min(58, end);
            } else if (letters) {
                start = Math.max(65, start);
                end = Math.min(123, end);
            }
        }
        StringBuilder builder = new StringBuilder(count);
        int gap = end - start;
        int gapBits = 32 - Integer.numberOfLeadingZeros(gap);
        long desiredCacheSize = ((long)count * (long)gapBits + 3L) / 5L + 10L;
        int cacheSize = (int)Math.min(desiredCacheSize, 429496739L);
        CachedRandomBits arb = new CachedRandomBits(cacheSize, random);
        block3: while (count-- != 0) {
            int codePoint;
            int randomValue = arb.nextBits(gapBits) + start;
            if (randomValue >= end) {
                ++count;
                continue;
            }
            if (chars == null) {
                codePoint = randomValue;
                switch (Character.getType(codePoint)) {
                    case 0: 
                    case 18: 
                    case 19: {
                        ++count;
                        continue block3;
                    }
                }
            } else {
                codePoint = chars[randomValue];
            }
            int numberOfChars = Character.charCount(codePoint);
            if (count == 0 && numberOfChars > 1) {
                ++count;
                continue;
            }
            if (letters && Character.isLetter(codePoint) || numbers && Character.isDigit(codePoint) || !letters && !numbers) {
                builder.appendCodePoint(codePoint);
                if (numberOfChars != 2) continue;
                --count;
                continue;
            }
            ++count;
        }
        return builder.toString();
    }

    @Deprecated
    public static String random(int count, String chars) {
        return RandomStringUtils.secure().next(count, chars);
    }

    @Deprecated
    public static String randomAlphabetic(int count) {
        return RandomStringUtils.secure().nextAlphabetic(count);
    }

    @Deprecated
    public static String randomAlphabetic(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextAlphabetic(minLengthInclusive, maxLengthExclusive);
    }

    @Deprecated
    public static String randomAlphanumeric(int count) {
        return RandomStringUtils.secure().nextAlphanumeric(count);
    }

    @Deprecated
    public static String randomAlphanumeric(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextAlphanumeric(minLengthInclusive, maxLengthExclusive);
    }

    @Deprecated
    public static String randomAscii(int count) {
        return RandomStringUtils.secure().nextAscii(count);
    }

    @Deprecated
    public static String randomAscii(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextAscii(minLengthInclusive, maxLengthExclusive);
    }

    @Deprecated
    public static String randomGraph(int count) {
        return RandomStringUtils.secure().nextGraph(count);
    }

    @Deprecated
    public static String randomGraph(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextGraph(minLengthInclusive, maxLengthExclusive);
    }

    @Deprecated
    public static String randomNumeric(int count) {
        return RandomStringUtils.secure().nextNumeric(count);
    }

    @Deprecated
    public static String randomNumeric(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextNumeric(minLengthInclusive, maxLengthExclusive);
    }

    @Deprecated
    public static String randomPrint(int count) {
        return RandomStringUtils.secure().nextPrint(count);
    }

    @Deprecated
    public static String randomPrint(int minLengthInclusive, int maxLengthExclusive) {
        return RandomStringUtils.secure().nextPrint(minLengthInclusive, maxLengthExclusive);
    }

    public static RandomStringUtils secure() {
        return SECURE;
    }

    public static RandomStringUtils secureStrong() {
        return SECURE_STRONG;
    }

    @Deprecated
    public RandomStringUtils() {
        this(SECURE_SUPPLIER);
    }

    private RandomStringUtils(Supplier<RandomUtils> random) {
        this.random = random;
    }

    public String next(int count) {
        return this.next(count, false, false);
    }

    public String next(int count, boolean letters, boolean numbers) {
        return this.next(count, 0, 0, letters, numbers);
    }

    public String next(int count, char ... chars) {
        if (chars == null) {
            return RandomStringUtils.random(count, 0, 0, false, false, null, this.random());
        }
        return RandomStringUtils.random(count, 0, chars.length, false, false, chars, this.random());
    }

    public String next(int count, int start, int end, boolean letters, boolean numbers) {
        return RandomStringUtils.random(count, start, end, letters, numbers, null, this.random());
    }

    public String next(int count, int start, int end, boolean letters, boolean numbers, char ... chars) {
        return RandomStringUtils.random(count, start, end, letters, numbers, chars, this.random());
    }

    public String next(int count, String chars) {
        if (chars == null) {
            return RandomStringUtils.random(count, 0, 0, false, false, null, this.random());
        }
        return this.next(count, chars.toCharArray());
    }

    public String nextAlphabetic(int count) {
        return this.next(count, true, false);
    }

    public String nextAlphabetic(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextAlphabetic(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    public String nextAlphanumeric(int count) {
        return this.next(count, true, true);
    }

    public String nextAlphanumeric(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextAlphanumeric(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    public String nextAscii(int count) {
        return this.next(count, 32, 127, false, false);
    }

    public String nextAscii(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextAscii(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    public String nextGraph(int count) {
        return this.next(count, 33, 126, false, false);
    }

    public String nextGraph(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextGraph(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    public String nextNumeric(int count) {
        return this.next(count, false, true);
    }

    public String nextNumeric(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextNumeric(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    public String nextPrint(int count) {
        return this.next(count, 32, 126, false, false);
    }

    public String nextPrint(int minLengthInclusive, int maxLengthExclusive) {
        return this.nextPrint(this.randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    private Random random() {
        return this.randomUtils().random();
    }

    private RandomUtils randomUtils() {
        return this.random.get();
    }

    public String toString() {
        return "RandomStringUtils [random=" + this.random() + "]";
    }
}

