package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.generator.config.dto.ArrayConfig;
import org.laoruga.dtogenerator.generator.config.dto.BooleanConfig;
import org.laoruga.dtogenerator.generator.config.dto.NumberConfig;
import org.laoruga.dtogenerator.generator.config.dto.StringConfig;
import org.laoruga.dtogenerator.generator.config.dto.datetime.ChronoUnitConfig;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("Inherited Dto Tests")
@Epic("INHERITED_DTO")
class InheritedDtoTests {

    static class Dto extends DtoSuper {
        @NumberRule()
        Integer upperInt;
        @StringRule()
        String upperString;
    }

    static class DtoSuper extends DtoSuperSuper {

        @CollectionRule(element = @Entry(stringRule = @StringRule))
        List<String> superList;
        @DateTimeRule
        LocalDateTime superDateTime;
    }

    static class DtoSuperSuper {

        @MapRule
        Map<String, Long> superSuperMap;
        @DecimalRule
        Double superSuperDouble;
    }

    @Test
    @DisplayName("Super Class Generation")
    void generationWithInheritanceAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.upperInt),
                () -> assertNotNull(dto.upperString)
        );
        assertAll(
                () -> assertNotNull(dto.superList),
                () -> assertNotNull(dto.superDateTime)
        );
        assertAll(
                () -> assertNotNull(dto.superSuperMap),
                () -> assertNotNull(dto.superSuperDouble)
        );
    }

    static class Dto_2 extends DtoSuper_2 {
        @NumberRule()
        Integer upperInt;
        @StringRule()
        String upperString;
        @NestedDtoRule
        DtoNested upperDtoNested;
    }

    static class DtoSuper_2 extends DtoSuperSuper_2 {

        @CollectionRule(element = @Entry(stringRule = @StringRule))
        List<String> superList;
        @DateTimeRule
        LocalDateTime superDateTime;
        @NestedDtoRule
        DtoNested superDtoNested;
    }

    static class DtoSuperSuper_2 {

        @MapRule
        Map<String, Long> superSuperMap;
        @DecimalRule
        Double superSuperDouble;
        @NestedDtoRule
        DtoNested superSuperDtoNested;
    }

    static class DtoNested extends DtoNestedAncestor {

        @ArrayRule
        String[] nestedArray;
        @BooleanRule
        Boolean nestedBoolean;
    }

    static class DtoNestedAncestor {

        @ArrayRule
        int[] nestedAncestorIntArray;
        @StringRule
        String nestedAncestorString;
    }

    @Test
    @DisplayName("Super Classes Generation With Inheritance And Nested DTOs")
    void generationWithInheritanceAndNestedDTOs() {
        Dto_2 dto = DtoGenerator.builder(Dto_2.class).build().generateDto();

        assertNotNull(dto);

        // Upper class
        assertAll(
                () -> assertNotNull(dto.upperInt),
                () -> assertNotNull(dto.upperString),
                () -> assertNotNull(dto.upperDtoNested.nestedArray),
                () -> assertNotNull(dto.upperDtoNested.nestedBoolean),
                () -> assertNotNull(dto.upperDtoNested.nestedAncestorIntArray),
                () -> assertNotNull(dto.upperDtoNested.nestedAncestorString)
        );

        // First Super Class
        assertAll(
                () -> assertNotNull(dto.superList),
                () -> assertNotNull(dto.superDateTime),
                () -> assertNotNull(dto.superDtoNested.nestedArray),
                () -> assertNotNull(dto.superDtoNested.nestedBoolean),
                () -> assertNotNull(dto.superDtoNested.nestedAncestorIntArray),
                () -> assertNotNull(dto.superDtoNested.nestedAncestorString)
        );

        // Second Super Class
        assertAll(
                () -> assertNotNull(dto.superSuperMap),
                () -> assertNotNull(dto.superSuperDouble),
                () -> assertNotNull(dto.superSuperDtoNested.nestedArray),
                () -> assertNotNull(dto.superSuperDtoNested.nestedBoolean),
                () -> assertNotNull(dto.superSuperDtoNested.nestedAncestorIntArray),
                () -> assertNotNull(dto.superSuperDtoNested.nestedAncestorString)
        );
    }

    @Test
    @DisplayName("Upper Class Configuring")
    void upperClassConfiguring() {

        final int[] INT_ARRAY = {1, 2, 3, 4, 5};
        final String STRING = "FOX";

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGeneratorConfig(Integer.class, NumberConfig.builder().maxValue(1).minValue(1).build())
                .setGeneratorConfig(String.class, StringConfig.builder().words(STRING).build())
                .setGenerator("upperDtoNested.nestedBoolean", () -> false)
                .setGenerator("upperDtoNested.nestedAncestorIntArray", () -> INT_ARRAY)
                .build().generateDto();

        assertAll(
                () -> assertThat("User's Type Config",
                        dto.upperInt, equalTo(1)),
                () -> assertThat("User's Type Config",
                        dto.upperString, equalTo("FOX")),
                () -> assertThat("User's Type Config for Array Element (Nested DTO)",
                        Arrays.asList(dto.upperDtoNested.nestedArray), everyItem(equalTo(STRING))),
                () -> assertThat("User's Generator For Field (Nested DTO)",
                        dto.upperDtoNested.nestedBoolean, is(false)),
                () -> assertThat("User's Generator For Super Field of Nested DTO",
                        dto.upperDtoNested.nestedAncestorIntArray, equalTo(INT_ARRAY)),
                () -> assertThat("User's Type Config (Super Field of Nested DTO)",
                        dto.upperDtoNested.nestedAncestorString, equalTo(STRING))
        );

    }

    @Test
    @DisplayName("Super Class Configuring")
    void superClassConfiguring() {

        final List<String> STRING_LIST = new LinkedList<>();
        final int[] INT_ARRAY = {1, 2, 3, 4, 5};

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGeneratorConfig(LocalDateTime.class, DateTimeConfig.builder().addChronoConfig(
                        ChronoUnitConfig.newAbsolute(10, ChronoUnit.DAYS)).build()
                )
                .setGeneratorConfig(Boolean.class, BooleanConfig.builder().trueProbability(1D).build())
                .setGeneratorConfig("superDtoNested.nestedArray",
                        ArrayConfig.builder().minSize(0).maxSize(0).build()
                )
                .setGenerator("superList", () -> STRING_LIST)
                .setGenerator("superDtoNested.nestedAncestorIntArray", () -> INT_ARRAY)
                .build().generateDto();

        // First Super Class
        assertAll(
                () -> assertThat("User's Generator For Field",
                        dto.superList, sameInstance(STRING_LIST)),
                () -> assertThat("User's Type Config",
                        dto.superDateTime.toLocalDate(), equalTo(LocalDate.now().plusDays(10))),
                () -> assertThat("User's Config For Nested Field",
                        dto.superDtoNested.nestedArray.length, equalTo(0)),
                () -> assertThat("User's Type Config For Nested Field",
                        dto.superDtoNested.nestedBoolean, is(true)),
                () -> assertThat("User's Generator For Super Field of Super Nested",
                        dto.superDtoNested.nestedAncestorIntArray, equalTo(INT_ARRAY)),
                () -> assertNotNull(dto.superDtoNested.nestedAncestorString)
        );

    }

    @Test
    @DisplayName("Super Class Of Super Class Configuring")
    void superClassOfSuperClassConfiguring() {

        final Map<String, Long> MAP = new HashMap<>();
        final Double DOUBLE = 77D;
        final int[] INT_ARRAY = {1, 2, 3, 4, 5};

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGenerator("superSuperMap", () -> MAP)
                .setGenerator(Double.class, () -> DOUBLE)
                .setGenerator("superSuperDtoNested.nestedAncestorIntArray", () -> INT_ARRAY)
                .setGeneratorConfig(Boolean.class, BooleanConfig.builder().trueProbability(0D).build())
                .setGeneratorConfig("superSuperDtoNested.nestedArray",
                        ArrayConfig.builder().minSize(0).maxSize(0).build()
                )
                .setGeneratorConfig(String.class, StringConfig.builder().minLength(1).maxLength(1).build())
                .build().generateDto();

        // Second Super Class
        assertAll(
                () -> assertThat("User's Generator For Field (Second Super Class)",
                        dto.superSuperMap, sameInstance(MAP)),
                () -> assertThat("User's Generator For Type (Second Super Class)",
                        dto.superSuperDouble, is(DOUBLE)),
                () -> assertThat("User's Config For Nested Field (Second Super Class)",
                        dto.superSuperDtoNested.nestedArray.length, is(0)),
                () -> assertThat("User's Type Config For Nested Field",
                        dto.superSuperDtoNested.nestedBoolean, is(false)),
                () -> assertThat("User's Generator For Super Field of Super Nested",
                        dto.superSuperDtoNested.nestedAncestorIntArray, equalTo(INT_ARRAY)),
                () -> assertThat("User's Generator For Type (Nested Dto of Second Super Class)",
                        dto.superSuperDtoNested.nestedAncestorString, hasLength(1))
        );
    }

}
