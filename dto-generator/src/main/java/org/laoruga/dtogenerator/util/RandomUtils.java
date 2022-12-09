package org.laoruga.dtogenerator.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.laoruga.dtogenerator.constants.CharSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Il'dar Valitov
 * Created on 10.05.2022
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomUtils {

    private static final RandomStringGenerator DEFAULT_STRING_GENERATOR =
            new RandomStringGenerator.Builder().selectFrom(CharSet.DEFAULT_CHARSET.toCharArray()).build();

    @Getter
    private static final Random random = new Random();

    public static Double nextDouble(int minNumber, int maxNumber) {
        double floatPart = random.nextDouble();
        int integerPart = nextInt(minNumber, maxNumber);
        return integerPart == maxNumber ? maxNumber : integerPart + floatPart;
    }

    /**
     * @param minNumber min value inclusive
     * @param maxNumber max value inclusive
     * @return random int
     */
    public static Integer nextInt(int minNumber, int maxNumber) {
        return random.nextInt((maxNumber - minNumber) + 1) + minNumber;
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
        return nextInt(0, 1) == 1;
    }

    /*
     * Receiving random item from list
     */

    /**
     * @param collection - collection to take a random element
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
     * @return - random element from array
     */
    public static <T> T getRandomItem(T... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Empty array passed");
        }
        return items[nextInt(0, items.length - 1)];
    }

    public static long nextLong(long minNumber, long maxNumber) {
        if (minNumber == maxNumber) {
            return minNumber;
        } else {
            return ThreadLocalRandom.current().nextLong(minNumber, maxNumber);
        }
    }

    public static String nextString(char[] chars, int length) {
        return new RandomStringGenerator.Builder()
                .selectFrom(chars).build().generate(length);
    }

    public static String nextString(int length) {
        return DEFAULT_STRING_GENERATOR.generate(length);
    }

}