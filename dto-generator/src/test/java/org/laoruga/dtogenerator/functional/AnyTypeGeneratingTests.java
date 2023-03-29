package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 29.03.2023
 */
@DisplayName("Any Type Generating Tests")
@Epic("ANY_TYPE_GENERATING")
@Slf4j
class AnyTypeGeneratingTests {


    static class Dto {

        Date harvestDate;
        Tomato tomato;

        // known types

        String stringAsKnownType;
        List<Integer> listOfIntegerAsKnownType;

    }

    @Value
    static class Tomato {
        Color color;
        int weight;
    }

    enum Color {
        RED, GREEN, BLUE
    }

    @Test
    void generatorByTypeForUnknownType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        builder.setGenerator(Tomato.class,
                        () -> new Tomato(RandomUtils.getRandomItem(Color.values()), RandomUtils.nextInt(1, Integer.MAX_VALUE)))
                .setGenerator(Date.class,
                        () -> Date.from(Instant.now().plusNanos(RandomUtils.nextLong())));

        DtoGenerator<Dto> dtoGenerator = builder.build();

        int uniqueDtoNumber = 5;
        List<Dto> dtoList = IntStream.range(0, uniqueDtoNumber).boxed()
                .map(i -> dtoGenerator.generateDto())
                .collect(Collectors.toList());

        Set<Tomato> differentTomatoes = dtoList.stream()
                .map(i -> i.tomato)
                .collect(Collectors.toSet());

        Set<Date> differentDates = dtoList.stream()
                .map(i -> i.harvestDate)
                .collect(Collectors.toSet());

        Set<String> differentString = dtoList.stream()
                .map(i -> i.stringAsKnownType)
                .collect(Collectors.toSet());

        Set<List<Integer>> differentLists = dtoList.stream()
                .map(i -> i.listOfIntegerAsKnownType)
                .collect(Collectors.toSet());

        assertAll(
                () -> assertThat(differentTomatoes, hasSize(uniqueDtoNumber)),
                () -> assertThat(differentDates, hasSize(uniqueDtoNumber)),
                () -> assertThat(differentString, hasSize(uniqueDtoNumber)),
                () -> assertThat(differentLists, hasSize(uniqueDtoNumber))

        );
    }

    @Test
    void generatorByFieldForUnknownType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        final Tomato TOMATO = new Tomato(null, 0);
        final Date DATE = Date.from(Instant.now());

        builder.setGenerator(Tomato.class,
                        () -> new Tomato(RandomUtils.getRandomItem(Color.values()), RandomUtils.nextInt(1, Integer.MAX_VALUE)))
                .setGenerator(Date.class,
                        () -> Date.from(Instant.now().plusNanos(RandomUtils.nextLong())))
                .setGenerator("tomato", () -> TOMATO)
                .setGenerator("harvestDate", () -> DATE);

        DtoGenerator<Dto> dtoGenerator = builder.build();

        int uniqueDtoNumber = 5;
        List<Dto> dtoList = IntStream.range(0, uniqueDtoNumber).boxed()
                .map(i -> dtoGenerator.generateDto())
                .collect(Collectors.toList());

        Set<Tomato> differentTomatoes = dtoList.stream()
                .map(i -> i.tomato)
                .collect(Collectors.toSet());

        Set<Date> differentDates = dtoList.stream()
                .map(i -> i.harvestDate)
                .collect(Collectors.toSet());

        Set<String> differentString = dtoList.stream()
                .map(i -> i.stringAsKnownType)
                .collect(Collectors.toSet());

        Set<List<Integer>> differentLists = dtoList.stream()
                .map(i -> i.listOfIntegerAsKnownType)
                .collect(Collectors.toSet());

        assertAll(
                () -> assertThat(differentTomatoes, hasSize(1)),
                () -> assertThat(differentDates, hasSize(1)),
                () -> assertThat(differentString, hasSize(uniqueDtoNumber)),
                () -> assertThat(differentLists, hasSize(uniqueDtoNumber))
        );
    }

}
