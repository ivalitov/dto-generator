package laoruga.dtogenerator.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomUtils.nextInt;

@Slf4j
public class RandomUtils {

    static final Random random = new Random();

    private static final String HEX_CHARSET = "0123456789abcdefABCDEF";

    public static Double nextDouble(int minNumber, int maxNumber) {
        double floatPart = random.nextDouble();
        int integerPart = nextInt(minNumber, maxNumber);
        return integerPart + floatPart;
    }

    public static Integer nextInt(int minNumber, int maxNumber) {
        return random.nextInt((maxNumber - minNumber) + 1) + minNumber;
    }

    public static boolean nextBoolean() {
        return nextInt(0, 1) == 1;
    }

    public static Integer[] nextIntArray(int length, int minValue, int maxValue) {
        List<Integer> possibleValues = IntStream.range(minValue, maxValue + 1)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(possibleValues);
        if (length > possibleValues.size()) {
            throw new IllegalArgumentException("Уникальных значений меньше чем " + length);
        }
        return possibleValues.subList(0, length).toArray(new Integer[0]);
    }

    /*
     * Receiving random item from list
     */

    public static <T> T getRandomItemOrNull(T... items) {
        if (items == null) {
            return null;
        }
        return getRandomItemOrNull(Arrays.asList(items));
    }

    public static <T> T getRandomItemOrNull(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        List<T> asList = (collection instanceof List) ? (List<T>) collection : new ArrayList<>(collection);
        return asList.get(RandomUtils.nextInt(0, asList.size() - 1));
    }

    /*
     * Методы возрващают случайный элемент, если элементов нет - возникает ошибка
     */
    public static <T> T getRandomItemFromList(T... items) {
        int index = org.apache.commons.lang3.RandomUtils.nextInt(0, items.length);
        return items[index];
    }

    public static <T> T getRandomItemFromList(List<T> list) {
        int index = org.apache.commons.lang3.RandomUtils.nextInt(0, list.size());
        return list.get(index);
    }

    public static <T> List<T> getRandomItemsFromList(List<T> list, Integer maxCount) {
        Set<T> set = new HashSet<>();
        for (int i = 0; i < maxCount; i++) {
            set.add(getRandomItemFromList(list));
        }
        return Arrays.asList((T[]) set.toArray());
    }

    // Генерация LONG

    public static long generateRandomLong(long minNumber, long maxNumber) {
        if (minNumber == maxNumber) {
            return minNumber;
        } else {
            return ThreadLocalRandom.current().nextLong(minNumber, maxNumber);
        }
    }

    public static String generateRandomAlphaNumericString(int stringLength) {
        return RandomStringUtils.randomAlphanumeric(stringLength);
    }

    public static String generateRandomAlphaString(int stringLength) {
        return RandomStringUtils.randomAlphabetic(stringLength);
    }

    public static String generateRandomHexNumericString(int stringLength) {
        return RandomStringUtils.random(stringLength, HEX_CHARSET);
    }

    public static String generateRandomNumericString(int stringLength) {
        return RandomStringUtils.randomNumeric(stringLength);
    }

    public static int getRandomDuration() {
        return (org.apache.commons.lang3.RandomUtils.nextInt(0, 120) + 1) * 60;
    }

    public static LocalDateTime getRandomDateTimeInCertainInterval(LocalDateTime startDate, LocalDateTime endDate) {
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(startDate);
        long start = startDate.toEpochSecond(offset);
        long finish = endDate.toEpochSecond(offset);
        long randomDate = RandomUtils.generateRandomLong(start, finish);
        return LocalDateTime.ofEpochSecond(randomDate, 0, offset);
    }

    public static LocalDateTime getRandomDateInCertainInterval(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime random = getRandomDateTimeInCertainInterval(startDate, endDate);
        return LocalDateTime.of(random.getYear(), random.getMonth(), random.getDayOfMonth(), 0, 0);
    }

}