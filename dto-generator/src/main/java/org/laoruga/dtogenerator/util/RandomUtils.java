package org.laoruga.dtogenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.text.RandomStringGenerator;
import org.laoruga.dtogenerator.constants.CharSet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * @author Il'dar Valitov
 * Created on 10.05.2022
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomUtils {

    private static final RandomStringGenerator DEFAULT_STRING_GENERATOR =
            new RandomStringGenerator.Builder().selectFrom(CharSet.DEFAULT_CHARSET.toCharArray()).build();

    public static final Random RANDOM = new Random();

    private static final RandomDataGenerator RANDOM_DATA_GENERATOR = new RandomDataGenerator();

    public static double nextDouble(double minValue, double maxValue, int precision) {
        double generated = minValue + RANDOM.nextDouble() * (maxValue - minValue);
        return Precision.round(generated, precision);
    }

    public static float nextFloat(float minValue, float maxValue, int precision) {
        float generated = minValue + RANDOM.nextFloat() * (maxValue - minValue);
        return BigDecimal.valueOf(generated)
                .setScale(precision, RoundingMode.HALF_UP)
                .floatValue();
    }

    /**
     * @param minNumber min value inclusive, may be negative
     * @param maxNumber max value inclusive, may be negative,
     *                  must be greater than minNumber or equal
     * @return random int
     */
    public static synchronized int nextInt(int minNumber, int maxNumber) {
        if (minNumber == maxNumber) {
            return minNumber;
        }
        return RANDOM_DATA_GENERATOR.nextInt(minNumber, maxNumber);
    }

    public static Short nextShort(short minNumber, short maxNumber) {
        return (short) nextInt(minNumber, maxNumber);
    }

    public static byte nextByte(byte minNumber, byte maxNumber) {
        return (byte) nextInt(minNumber, maxNumber);
    }

    /**
     * @param minNumber min value inclusive
     * @param maxNumber max value inclusive
     * @return random int
     */
    public static Integer nextInt(String minNumber, String maxNumber) {
        return nextInt(Integer.parseInt(minNumber), Integer.parseInt(maxNumber));
    }

    public static boolean nextBoolean() {
        return RANDOM.nextInt(2) == 1;
    }

    /*
     * Receiving random item from list
     */

    /**
     * @param collection - collection to take a random element
     * @param <T>        collection element type
     * @return - random element from collection
     */
    public static <T> T getRandomItem(Collection<T> collection) {
        Iterator<T> iterator = collection.iterator();
        int randomIdx = nextInt(0, collection.size() - 1);
        int idx = 0;
        T result = iterator.next();
        while (iterator.hasNext() && randomIdx != idx) {
            result = iterator.next();
            idx++;
        }
        return result;
    }

    /**
     * @param items - array to take a random element
     * @param <T>   array element type
     * @return - random element from array
     */
    public static <T> T getRandomItem(T... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Empty array passed");
        }
        return items[nextInt(0, items.length - 1)];
    }

    public static int getRandomItem(int... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Empty int array passed");
        }
        return items[nextInt(0, items.length - 1)];
    }

    public static synchronized long nextLong(long minNumber, long maxNumber) {
        if (minNumber == maxNumber) {
            return minNumber;
        }
        return RANDOM_DATA_GENERATOR.nextLong(minNumber, maxNumber);
    }

    public static synchronized long nextLong() {
        return RANDOM.nextLong();
    }

    public static Number nextNumber(Number minNumber, Number maxNumber) {
        if (minNumber.equals(maxNumber)) {
            return minNumber;
        } else {
            if (minNumber.getClass() == Integer.class &&
                    maxNumber.getClass() == Integer.class) {
                return nextInt((Integer) minNumber, (Integer) maxNumber);
            }
            if (minNumber.getClass() == Long.class &&
                    maxNumber.getClass() == Long.class) {
                return nextLong((Long) minNumber, (Long) maxNumber);
            }
            if (minNumber.getClass() == Short.class &&
                    maxNumber.getClass() == Short.class) {
                return nextShort((Short) minNumber, (Short) maxNumber);
            }
            if (minNumber.getClass() == Byte.class &&
                    maxNumber.getClass() == Byte.class) {
                return nextByte((Byte) minNumber, (Byte) maxNumber);
            }
            if (minNumber.getClass() == BigInteger.class &&
                    maxNumber.getClass() == BigInteger.class) {
                return nextBigInteger((BigInteger) minNumber, (BigInteger) maxNumber);
            }
            throw new IllegalArgumentException("Unexpected number instances of classes: " +
                    "'" + minNumber.getClass() + "' and '" + maxNumber.getClass() + "'");
        }
    }

    public static Number nextNumberDecimal(Number minNumber, Number maxNumber, int precision) {
        if (minNumber.equals(maxNumber)) {
            return minNumber;
        } else {
            if (minNumber.getClass() == Double.class &&
                    maxNumber.getClass() == Double.class) {
                return nextDouble((Double) minNumber, (Double) maxNumber, precision);
            }
            if (minNumber.getClass() == Float.class &&
                    maxNumber.getClass() == Float.class) {
                return nextFloat((Float) minNumber, (Float) maxNumber, precision);
            }
            if (minNumber.getClass() == BigDecimal.class &&
                    maxNumber.getClass() == BigDecimal.class) {
                return nextBigDecimal((BigDecimal) minNumber, (BigDecimal) maxNumber, precision);
            }
            throw new IllegalArgumentException("Unexpected decimal number instances of classes: " +
                    "'" + minNumber.getClass() + "' and '" + maxNumber.getClass() + "'");
        }
    }


    public static String nextString(char[] chars, int length) {
        return new RandomStringGenerator.Builder()
                .selectFrom(chars).build().generate(length);
    }

    public static String nextString(int length) {
        return DEFAULT_STRING_GENERATOR.generate(length);
    }

    public static BigDecimal nextBigDecimal(BigDecimal minValue, BigDecimal maxValue, int precision) {
        BigDecimal randomBigDecimal = minValue.add(
                BigDecimal.valueOf(Math.random()).multiply(maxValue.subtract(minValue))
        );
        return randomBigDecimal.setScale(precision, RoundingMode.HALF_UP);
    }

    public static BigInteger nextBigInteger(BigInteger minValue, BigInteger maxValue) {
        return nextBigDecimal(new BigDecimal(minValue), new BigDecimal(maxValue), 0).toBigInteger();
    }

}