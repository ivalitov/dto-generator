package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

    @EqualsAndHashCode
    @RequiredArgsConstructor
    @AllArgsConstructor
    static class Tomato {
        final Color color;
        final int weight;
        String comment;
    }

    enum Color {
        RED, GREEN, BLUE
    }

    @Test
    void generatorByTypeForUnknownType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);
        builder.getConfig().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        builder.setGenerator(Tomato.class,
                        () -> new Tomato(
                                RandomUtils.getRandomItem(Color.values()),
                                RandomUtils.nextInt(1, Integer.MAX_VALUE)))
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
                        () -> new Tomato(
                                RandomUtils.getRandomItem(Color.values()),
                                RandomUtils.nextInt(1, Integer.MAX_VALUE)))
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

    static class TomatoGenerator implements
            CustomGeneratorArgs<Tomato>,
            CustomGeneratorDtoDependent<Tomato, Dto> {

        List<Color> colors = new LinkedList<>();
        List<Integer> weights = new LinkedList<>();

        Supplier<Dto> generatedDto;

        @Override
        public Tomato generate() {
            return new Tomato(
                    RandomUtils.getRandomItem(colors),
                    RandomUtils.getRandomItem(weights),
                    generatedDto.get().stringAsKnownType
            );
        }

        @Override
        public void setArgs(String... args) {
            for (String arg : args) {
                try {
                    colors.add(Color.valueOf(arg));
                } catch (Exception e) {
                    weights.add(Integer.valueOf(arg));
                }
            }
            if (colors.isEmpty() || weights.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void setDtoSupplier(Supplier<Dto> generatedDto) {
            this.generatedDto = generatedDto;
        }

        @Override
        public boolean isDtoReady() {
            return generatedDto.get().stringAsKnownType != null;
        }
    }

    @Test
    void customGeneratorByTypeForUnknownType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class).generateKnownTypes();

        builder.setGenerator(Tomato.class, new TomatoGenerator(), "RED", "GREEN", "BLUE", "33", "25", "100");

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.harvestDate, nullValue()),
                () -> assertThat(dto.stringAsKnownType, notNullValue()),
                () -> assertThat(dto.tomato.comment, equalTo(dto.stringAsKnownType)),
                () -> assertThat(dto.listOfIntegerAsKnownType, notNullValue())
        );
    }

    @Test
    void customGeneratorByFieldForUnknownType() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class).generateKnownTypes();

        builder.setGenerator("tomato", new TomatoGenerator(), "RED", "100");

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.harvestDate, nullValue()),
                () -> assertThat(dto.stringAsKnownType, notNullValue()),
                () -> assertThat(dto.tomato.comment, equalTo(dto.stringAsKnownType)),
                () -> assertThat(dto.tomato.color, equalTo(Color.RED)),
                () -> assertThat(dto.tomato.weight, equalTo(100)),
                () -> assertThat(dto.listOfIntegerAsKnownType, notNullValue())
        );
    }

}
