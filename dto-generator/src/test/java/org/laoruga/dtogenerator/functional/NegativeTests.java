package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.laoruga.dtogenerator.constants.Group.GROUP_1;

/**
 * @author Il'dar Valitov
 * Created on 27.11.2022
 */
@DisplayName("Negative tests")
@Epic("NEGATIVE_TESTS")
public class NegativeTests {

    static class DtoNegative1 {
        @NumberRule
        String string;
    }

    static class DtoNegative2 {
        @StringRule
        @StringRule(group = GROUP_1)
        Long loong;
    }

    static Stream<Arguments> unappropriatedDataSet() {
        return Stream.of(
                Arguments.of("string",
                        NegativeTests.DtoNegative1.class,
                        "'class java.lang.String' does not match to rules annotation: '@NumberRule'"),
                Arguments.of("loong",
                        NegativeTests.DtoNegative2.class,
                        "'class java.lang.Long' does not match to rules annotation: '@StringRule'"));
    }

    @ParameterizedTest
    @MethodSource("unappropriatedDataSet")
    @DisplayName("Wrong rule annotation")
    void wrongRule(String fieldName, Class<?> dtoClass, String errMsgPart) {
        DtoGenerator<?> generator = DtoGenerator.builder(dtoClass).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);
        Throwable exception = UtilsRoot.getErrorsMap(generator).get(fieldName);
        assertThat(exception.getCause().getMessage(),
                containsString(errMsgPart));
    }

}
