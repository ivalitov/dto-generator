package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.StringConfig;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.laoruga.dtogenerator.constants.CharSet.*;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("STRING_RULES")
class StringGenerationTests {

    @Getter
    static class Dto {
        @StringRule(regexp = "[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}")
        private String phoneNum;
        @StringRule(regexp = "[a-zA-Z] [+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2} [а-яёА-ЯЁ]{2}")
        private String phoneNumLetters;
    }

    @Getter
    static class Dto_2 {
        @StringRule
        private String string;
    }

    static class Dto_3 {
        @StringRule(words = {"one", "two", "three"})
        private String string;
    }


    @RepeatedTest(5)
    @DisplayName("Generated string by mask (phone number)")
    void maskPhoneNumber() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();
        assertAll(
                () -> assertThat(dto.getPhoneNum(), hasLength(19)),
                () -> assertThat(dto.getPhoneNum(), matchesRegex("^[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}$")),

                () -> assertThat(dto.getPhoneNumLetters(), hasLength(24)),
                () -> assertThat(dto.getPhoneNumLetters(), matchesRegex("^[a-zA-Z] [+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2} [а-яёА-ЯЁ]{2}$"))
        );
        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getPhoneNum())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[%][0-9][%]{5}[0-9][%]",
            "[0-9][%]{7}[0-9]",
            "[0-9]{3}[%]{3}[0-9]{3}",
            "[%]{3}[0-9]{3}[%]{3}",
            "[%][0-9]{1}",
            "[0-9]{2}[%]",
            "[0-9]{3}",
            "[%]{3}",})
    @DisplayName("Generated string by regexp")
    void generateByRegexp(String regexp) {

        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGeneratorConfig(String.class,
                        StringConfig.builder()
                                .regexp(regexp)
                                .chars(NUM).build())
                .build().generateDto();
        assertAll(
                () -> assertThat(dto.getString(), matchesRegex("^" + regexp + "$"))
        );
    }


    public static Stream<Arguments> regexpAndLengthBounds() {
        return Stream.of(
                Arguments.of(NUM, 50, 50, "\\d*"),
                Arguments.of(ENG, 1, 15, "[a-z]*"),
                Arguments.of(ENG_CAP, 0, 28, "[A-Z]*"),
                Arguments.of(RUS, 10, 10, "[а-яё]*"),
                Arguments.of(RUS_CAP, 1, 1, "[А-ЯЁ]*")
        );
    }

    @ParameterizedTest
    @MethodSource("regexpAndLengthBounds")
    @DisplayName("Generated string by mask (type chars + wildcard)")
    void generateByLength(String charSet, Integer minLength, Integer maxLength, String regexpForAssert) {
        Dto_2 dto = DtoGenerator.builder(Dto_2.class)
                .setGeneratorConfig(String.class,
                        StringConfig.builder()
                                .minLength(minLength)
                                .maxLength(maxLength)
                                .chars(charSet).build())
                .build().generateDto();
        assertAll(
                () -> assertThat(dto.getString().length(), both(lessThanOrEqualTo(maxLength))
                        .and(greaterThanOrEqualTo(minLength))),
                () -> assertThat(dto.getString(), matchesRegex("^" + regexpForAssert + "$"))
        );
    }

    @Tag("NEGATIVE_TEST")
    @Test
    @DisplayName("Wrong Bounds")
    void wrongBounds() {
        DtoGenerator<Dto> builder_1 = DtoGenerator.builder(Dto.class)
                .setGeneratorConfig("phoneNum", StringConfig.builder().maxLength(2).build())
                .setGeneratorConfig("phoneNumLetters", StringConfig.builder().minLength(50).build())
                .build();

        assertThrows(DtoGeneratorException.class, builder_1::generateDto);

        DtoGenerator<Dto> builder_2 = DtoGenerator.builder(Dto.class)
                .setGeneratorConfig("phoneNumLetters", StringConfig.builder().minLength(50).build())
                .build();

        assertThrows(DtoGeneratorException.class, builder_2::generateDto);


        assertThrows(DtoGeneratorException.class, () -> DtoGenerator.builder(Dto_3.class)
                .setGeneratorConfig("string", StringConfig.builder().minLength(5).build())
                .build());

        assertThrows(DtoGeneratorException.class, () -> DtoGenerator.builder(Dto_3.class)
                .setGeneratorConfig("string", StringConfig.builder().maxLength(1).build())
                .build());
    }

}
