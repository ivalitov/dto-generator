package laoruga.dtogenerator.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static laoruga.dtogenerator.api.constants.CharSet.NUM;
import static laoruga.dtogenerator.api.tests.util.TestUtils.getField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 14.11.2022
 */
@DisplayName("RulesInfoExtractor tests")
@Epic("RULES_INFO_EXTRACTOR")
public class RulesInfoExtractorTests {

    static class Dto {
        @StringRule(minSymbols = 1, maxSymbols = 1, charset = NUM)
        String string;
        @IntegerRule
        int integer;
        @DoubleRule
        Double decimal;
        @LongRule
        long loong;
        @LocalDateTimeRule
        LocalDateTime localDateTime;
    }

    static RulesInfoExtractor getExctractorInstance() {
        return new RulesInfoExtractor(new FieldGroupFilter());
    }

    static Stream<Arguments> dataSet() {
        return Stream.of(
                Arguments.of(Dto.class, "string", StringRule.class),
                Arguments.of(Dto.class, "integer", IntegerRule.class),
                Arguments.of(Dto.class, "decimal", DoubleRule.class),
                Arguments.of(Dto.class, "loong", LongRule.class),
                Arguments.of(Dto.class, "localDateTime", LocalDateTimeRule.class)
        );
    }

    @ParameterizedTest
    @MethodSource("dataSet")
    @Feature("RULES_INFO_EXTRACTING")
    @DisplayName("General rules info extracting")
    void generalRulesInfo(Class<?> dtoClass, String fieldName, Class<?> rulesClass) {
        RulesInfoExtractor rulesInfoExtractor = getExctractorInstance();
        AnnotationErrorsHandler.ResultDto resultDto =
                new AnnotationErrorsHandler.ResultDto(1, 0, 0, 0);

        Field string = getField(dtoClass, fieldName);
        Optional<IRuleInfo> iRuleInfo = rulesInfoExtractor.checkAndWrapAnnotations(string, resultDto);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfo.class, iRuleInfo.get());

        RuleInfo ruleInfo = (RuleInfo) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(rulesClass)),
                () -> assertThat(ruleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(ruleInfo.isMultipleRules(), equalTo(false)),
                () -> assertThat(ruleInfo.getGroup(), equalTo("DEFAULT"))
        );

    }
}
