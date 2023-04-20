package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 29.03.2023
 */
@DisplayName("Ignoring Fields")
@Epic("IGNORING_FIELDS")
@Slf4j
class IgnoringFieldsTests {

    static class Dto {

        String string;
        Integer integer;
        Set<Long> setOfLong;
        ClientType clientType;

        @NestedDtoRule
        NestedDto nestedDto;
    }

    static class NestedDto {
        Long aLong;
        Double aDouble;
        Boolean aBoolean;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;
    }

    @Test
    void ignoreFieldsKnownTypesGeneration() {

        Dto dto = DtoGenerator.builder(Dto.class)
                .generateKnownTypes()
                .ignoreField("string")
                .ignoreField("integer")
                .ignoreField("nestedDto.aLong")
                .ignoreField("nestedDto.aDouble")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto.string, nullValue()),
                () -> assertThat(dto.integer, nullValue()),
                () -> assertThat(dto.clientType, notNullValue()),
                () -> assertThat(dto.setOfLong.size(), notNullValue())
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.aLong, nullValue()),
                () -> assertThat(nestedDto.aDouble, nullValue()),
                () -> assertThat(nestedDto.localDateTime, notNullValue()),
                () -> assertThat(nestedDto.clientType, notNullValue()),
                () -> assertThat(nestedDto.listOfString.size(), notNullValue()),
                () -> assertThat(nestedDto.aBoolean, notNullValue())
        );

    }

    static class Dto_2 {

        @StringRule
        String string;
        @IntegralRule
        Integer integer;
        @CollectionRule
        Set<Long> setOfLong;
        @EnumRule
        ClientType clientType;

        @NestedDtoRule
        NestedDto_2 nestedDto;
    }

    static class NestedDto_2 {
        @IntegralRule
        Long aLong;
        @DecimalRule
        Double aDouble;
        @EnumRule
        ClientType clientType;
        @ArrayRule
        String[] arrayOfString;
    }

    @Test
    void ignoreAnnotatedFields() {

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .ignoreField("string")
                .ignoreField("setOfLong")
                .ignoreField("nestedDto")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto.string, nullValue()),
                () -> assertThat(dto.integer, notNullValue()),
                () -> assertThat(dto.setOfLong, nullValue()),
                () -> assertThat(dto.clientType, notNullValue()),
                () -> assertThat(dto.nestedDto, nullValue())
        );

        Dto_2 dto_2 = DtoGenerator.builder(Dto_2.class)
                .ignoreField("integer")
                .ignoreField("nestedDto.aDouble")
                .ignoreField("nestedDto.arrayOfString")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto_2.string, notNullValue()),
                () -> assertThat(dto_2.integer, nullValue()),
                () -> assertThat(dto_2.setOfLong, notNullValue()),
                () -> assertThat(dto_2.clientType, notNullValue()),
                () -> assertThat(dto_2.nestedDto, notNullValue())
        );

        NestedDto_2 nestedDto = dto_2.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.aLong, notNullValue()),
                () -> assertThat(nestedDto.aDouble, nullValue()),
                () -> assertThat(nestedDto.clientType, notNullValue()),
                () -> assertThat(nestedDto.arrayOfString, nullValue())
        );
    }

    static class Dto_3 {

        @StringRule(words = "HELLO")
        String stringAnnotated;

        String string;

        @CustomRule(generatorClass = FooGenerator.class)
        Foo fooAnnotated;

        Foo foo;
    }

    static class Foo {
    }

    static class FooGenerator implements CustomGenerator<Foo> {
        @Override
        public Foo generate() {
            return new Foo();
        }
    }

    @Test
    void ignoreWhenUsersGeneratorExists() {

        Dto_3 dto = DtoGenerator.builder(Dto_3.class)
                .setGenerator(Foo.class, Foo::new)
                .setGenerator(String.class, () -> "GOOD_BYE")
                .ignoreField("fooAnnotated")
                .ignoreField("foo")
                .ignoreField("stringAnnotated")
                .ignoreField("string")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto.stringAnnotated, nullValue()),
                () -> assertThat(dto.string, nullValue()),
                () -> assertThat(dto.fooAnnotated, nullValue()),
                () -> assertThat(dto.foo, nullValue())
        );

        Dto_3 dto_2 = DtoGenerator.builder(Dto_3.class)
                .setGenerator("foo", Foo::new)
                .setGenerator("fooAnnotated", Foo::new)
                .setGenerator("string", () -> "GOOD_BYE")
                .setGenerator("stringAnnotated", () -> "GOOD_GOOD_BYE")
                .ignoreField("fooAnnotated")
                .ignoreField("foo")
                .ignoreField("stringAnnotated")
                .ignoreField("string")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto_2.stringAnnotated, nullValue()),
                () -> assertThat(dto_2.string, nullValue()),
                () -> assertThat(dto_2.fooAnnotated, nullValue()),
                () -> assertThat(dto_2.foo, nullValue())
        );

    }

    @Test
    void ignoreWhenUsersGeneratorForNestedDto() {

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGenerator(Long.class, () -> 1L)
                .setGenerator(Double.class, () -> 2D)
                .setGenerator("nestedDto.arrayOfString", () -> new String[0])
                .setGenerator("nestedDto.clientType", () -> ClientType.LEGAL_PERSON)
                .ignoreField("nestedDto.aLong")
                .ignoreField("nestedDto.aDouble")
                .ignoreField("nestedDto.arrayOfString")
                .ignoreField("nestedDto.clientType")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto.string, notNullValue()),
                () -> assertThat(dto.integer, notNullValue()),
                () -> assertThat(dto.setOfLong, notNullValue()),
                () -> assertThat(dto.clientType, notNullValue()),
                () -> assertThat(dto.nestedDto, notNullValue())
        );

        NestedDto_2 nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.aLong, nullValue()),
                () -> assertThat(nestedDto.aDouble, nullValue()),
                () -> assertThat(nestedDto.clientType, nullValue()),
                () -> assertThat(nestedDto.arrayOfString, nullValue())
        );

    }

    @Test
    void doNotLoseExistedFieldValuesWhenIgnoring() {

        String string = "mate";
        ClientType clientType = ClientType.ORG;
        Integer integer = 777;
        Set<Long> setOfLong = new HashSet<>(Collections.singletonList(999L));
        NestedDto_2 nested = new NestedDto_2();
        Double aDouble = 10D;
        String[] arrayOfString = new String[]{"mice"};
        Long aLong = 11L;

        Dto_2 dto = new Dto_2();
        dto.string = string;
        dto.clientType = clientType;
        dto.integer = integer;
        dto.setOfLong = setOfLong;
        dto.nestedDto = nested;
        nested.aDouble = aDouble;
        nested.clientType = clientType;
        nested.arrayOfString = arrayOfString;
        nested.aLong = aLong;

        DtoGenerator.builder(dto)
                .ignoreField("string")
                .ignoreField("clientType")
                .ignoreField("setOfLong")
                .ignoreField("nestedDto.aLong")
                .ignoreField("nestedDto.arrayOfString")
                .ignoreField("nestedDto.clientType")
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dto.string, equalTo(string)),
                () -> assertThat(dto.integer, not(equalTo(integer))),
                () -> assertThat(dto.setOfLong, equalTo(setOfLong)),
                () -> assertThat(dto.clientType, equalTo(clientType)),
                () -> assertThat(dto.nestedDto, notNullValue())
        );

        NestedDto_2 nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.aLong, equalTo(aLong)),
                () -> assertThat(nestedDto.aDouble, not(equalTo(aDouble))),
                () -> assertThat(nestedDto.clientType, equalTo(clientType)),
                () -> assertThat(nestedDto.arrayOfString, equalTo(arrayOfString))
        );

    }

}
