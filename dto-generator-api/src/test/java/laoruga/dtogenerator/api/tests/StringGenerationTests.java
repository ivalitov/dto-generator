package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static laoruga.dtogenerator.api.constants.CharSet.NUM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("STRING_RULES")
public class StringGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @StringRules(mask = "+89 (***) ***-**-**", charset = NUM)
        private String phoneNum;
        @StringRules(mask = "%ENG%* +89 (***) ***-**-** %RUS%**", charset = NUM)
        private String phoneNumLetters;
    }

    @Getter
    @NoArgsConstructor
    static class Dto_2 {
        @StringRules
        private String string;
    }


    @RepeatedTest(1)
    @DisplayName("Generated string by mask (phone number)")
    public void maskPhoneNumber() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertAll(
                () -> assertThat(dto.getPhoneNum(), hasLength(19)),
                () -> assertThat(dto.getPhoneNum(), matchesRegex("[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}")),

                () -> assertThat(dto.getPhoneNumLetters(), hasLength(24)),
                () -> assertThat(dto.getPhoneNum(), matchesRegex("[a-zA-Z] [+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2} [а-яА-Я]{2}"))
        );
        dto.getPhoneNumLetters();
        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getPhoneNum())
        );
    }

    public static Stream<Arguments> maskTypeCharsAndWildCardSource() {
        return Stream.of(
                Arguments.of("%*%%%%%*%", "^[%][0-9][%]{5}[0-9][%]$"),
                Arguments.of("*%%%%%%%*", "^[0-9][%]{7}[0-9]$"),
                Arguments.of("***%%%***", "^[0-9]{3}[%]{3}[0-9]{3}$"),
                Arguments.of("%%%***%%%", "^[%]{3}[0-9]{3}[%]{3}$"),
                Arguments.of("%*", "^[%][0-9]{1}$"),
                Arguments.of("**%", "^[0-9]{2}[%]$"),
                Arguments.of("***", "^[0-9]{3}$"),
                Arguments.of("%%%", "^[%]{3}$")
        );
    }

    @ParameterizedTest
    @MethodSource("maskTypeCharsAndWildCardSource")
    @DisplayName("Generated string by mask (type chars + wildcard)")
    public void maskTypeCharsAndWildCard(String mask, String regexpCheck) {
        Dto_2 dto = DtoGenerator.builder()
                .setGeneratorForField("string",
                        BasicGeneratorsBuilders.stringBuilder()
                                .mask(mask)
                                .charset(NUM))
                .build().generateDto(Dto_2.class);
        assertAll(
                () -> assertThat(dto.getString(), matchesRegex(regexpCheck))
        );
    }

    public static Stream<Arguments> maskDifferentCharsetsSource() {
        return Stream.of(
                Arguments.of("num:*** eng:%ENG%*** rus:%RUS%***;", "^num:[0-9]{3} eng:[a-zA-Z]{3} rus:[а-яА-Я]{3};$"),
                Arguments.of("%ENG%*%%%%%*%RUS%*", "^[a-zA-Z]{1}[%]{5}[0-9][а-яА-Я]$"),
                Arguments.of("%%ENG%*%%%%%*%RUS%*%*%", "^[%][a-zA-Z]{1}[%]{5}[0-9][а-яА-Я][%][0-9][%]$")
        );
    }

    @ParameterizedTest
    @MethodSource("maskDifferentCharsetsSource")
    @DisplayName("Generated string by mask (compound types)")
    public void maskDifferentCharsets(String mask, String regexpCheck) {
        Dto_2 dto = DtoGenerator.builder()
                .setGeneratorForField("string",
                        BasicGeneratorsBuilders.stringBuilder()
                                .mask(mask)
                                .charset(NUM))
                .build().generateDto(Dto_2.class);
        assertAll(
                () -> assertThat(dto.getString(), matchesRegex(regexpCheck))
        );
    }

    @Test
    @DisplayName("Generated string by mask (custom wildcard and type symbols)")
    public void maskDifferentMarker() {
        Dto_2 dto = DtoGenerator.builder()
                .setGeneratorForField("string",
                        BasicGeneratorsBuilders.stringBuilder()
                                .maskWildcard('^')
                                .maskTypeMarker('#')
                                .mask("%ENG%* (^^^) ^^^-^^-^^ #RUS#^^^ *** %%%")
                                .charset(NUM))
                .build().generateDto(Dto_2.class);
        assertAll(
                () -> assertThat(dto.getString(), matchesRegex("^%ENG%[*] [(][0-9]{3}[)] [0-9]{3}[-][0-9]{2}[-][0-9]{2} [а-яА-Я]{3} [*]{3} [%]{3}$"))
        );
    }

}
