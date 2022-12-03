package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.functional.util.TestUtils;

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
        @IntegerRule
        String string;
    }

    static class DtoNegative2 {
        @IntegerRule
        @IntegerRule(group = GROUP_1)
        Long loong;
    }

    static Stream<Arguments> unappropriatedDataSet() {
        return Stream.of(
                Arguments.of("string", NegativeTests.DtoNegative1.class, "builder's generated type: 'class java.lang.Integer' not matched to field type: class java.lang.String'"),
                Arguments.of("loong", NegativeTests.DtoNegative2.class, "builder's generated type: 'class java.lang.Integer' not matched to field type: class java.lang.Long'"));
    }

    @ParameterizedTest
    @MethodSource("unappropriatedDataSet")
    @DisplayName("Wrong rule annotation")
    void wrongRule(String fieldName, Class<?> dtoClass, String errMsgPart) {
        DtoGenerator<?> generator = DtoGenerator.builder(dtoClass).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);
        Throwable exception = TestUtils.getErrorsMap(generator).get(fieldName);
        assertThat(exception.getMessage(),
                containsString(errMsgPart));
    }

}
