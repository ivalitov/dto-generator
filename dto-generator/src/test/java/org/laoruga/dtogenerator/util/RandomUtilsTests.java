package org.laoruga.dtogenerator.util;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */

@DisplayName("Random utils tests")
@Epic("UNIT_TESTS")
@Feature("RANDOM_UTILS")
public class RandomUtilsTests {

    @RepeatedTest(10)
    @DisplayName("Random double")
    void nextDouble() {
        assertThat(
                RandomUtils.nextDouble(1, 2),
                both(greaterThanOrEqualTo(1D))
                        .and(lessThanOrEqualTo(2D)));

        assertThat(
                RandomUtils.nextDouble(500, 1000),
                both(greaterThanOrEqualTo(500D))
                        .and(lessThanOrEqualTo(1000D)));

        assertThat(
                RandomUtils.nextDouble(100, 100),
                equalTo(100D));
    }

    @RepeatedTest(10)
    @DisplayName("Random integer")
    void nextInteger() {
        assertThat(
                RandomUtils.nextInt(1, 2),
                both(greaterThanOrEqualTo(1))
                        .and(lessThanOrEqualTo(2)));

        assertThat(
                RandomUtils.nextInt(500, 1000),
                both(greaterThanOrEqualTo(500))
                        .and(lessThanOrEqualTo(1000)));

        assertThat(
                RandomUtils.nextInt(100, 100),
                equalTo(100));
    }

    @RepeatedTest(10)
    @DisplayName("Random integer as string")
    void nextIntegerAsString() {
        assertThat(
                RandomUtils.nextInt("1", "2"),
                both(greaterThanOrEqualTo(1))
                        .and(lessThanOrEqualTo(2)));

        assertThat(
                RandomUtils.nextInt("500", "1000"),
                both(greaterThanOrEqualTo(500))
                        .and(lessThanOrEqualTo(1000)));

        assertThat(
                RandomUtils.nextInt("100", "100"),
                equalTo(100));
    }

    @RepeatedTest(10)
    @DisplayName("Random long")
    void nextLong() {
        assertThat(
                RandomUtils.nextLong(1L, 2L),
                both(greaterThanOrEqualTo(1L))
                        .and(lessThanOrEqualTo(2L)));

        assertThat(
                RandomUtils.nextLong(500L, 1000L),
                both(greaterThanOrEqualTo(500L))
                        .and(lessThanOrEqualTo(1000L)));

        assertThat(
                RandomUtils.nextLong(100L, 100L),
                equalTo(100L));
    }

    @RepeatedTest(10)
    @DisplayName("Get random item from collection")
    void getRandomItemFromCollection() {
        List<String> three = Arrays.asList("one", "two", "three");
        Set<String> items = IntStream.range(0, 100).boxed()
                .map(i -> RandomUtils.getRandomItem(three))
                .collect(Collectors.toSet());

        assertThat(items, containsInAnyOrder(three.toArray()));
    }

    @RepeatedTest(10)
    @DisplayName("Get random item from array")
    void getRandomItemFromArray() {
        Set<String> items = IntStream.range(0, 100).boxed()
                .map(i -> RandomUtils.getRandomItem("one", "two", "three"))
                .collect(Collectors.toSet());

        assertThat(items, containsInAnyOrder("one", "two", "three"));
    }

    @RepeatedTest(10)
    @DisplayName("Random string from chars")
    void nextStringFromChars() {
        String randomString = RandomUtils.nextString(new char[]{'a', 'b', 'c'}, 100);
        assertThat(randomString, matchesRegex("[abc]{100}"));
    }

    @RepeatedTest(10)
    @DisplayName("Random string")
    void nextString() {
        String randomString = RandomUtils.nextString(100);
        assertThat(randomString, matchesRegex(".{100}"));
    }

    @DisplayName("Random boolean")
    void nextBoolean() {
        Set<Boolean> result = IntStream.range(1, 50).boxed()
                .map(i -> RandomUtils.nextBoolean())
                .collect(Collectors.toSet());
        assertThat(result, containsInAnyOrder(true, false));
    }

}
