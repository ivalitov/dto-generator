package laoruga.dtogenerator.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.markup.rules.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static laoruga.dtogenerator.api.constants.CharSet.NUM;
import static laoruga.dtogenerator.api.constants.Group.*;
import static laoruga.dtogenerator.api.tests.util.TestUtils.getField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 14.11.2022
 */
@DisplayName("RulesInfoExtractor tests")
@Epic("UNIT_TESTS")
@Feature("RULES_INFO_EXTRACTOR")
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

        @ListRule
        @LocalDateTimeRule
        List<LocalDateTime> listOfDates;
        @SetRule
        @IntegerRule
        Set<Integer> setOfInts;

        @ListRule(group = GROUP_1)
        @StringRule(group = GROUP_1)
        @ListRule
        @StringRule
        List<String> listOfStringMultipleRules;

        @StringRule(group = GROUP_3)
        @StringRule(group = GROUP_2)
        @StringRule
        String stringMultipleRules;

    }

    static RulesInfoExtractor getExtractorInstance(String... groups) {
        return new RulesInfoExtractor(new FieldGroupFilter(groups));
    }

    static AnnotationErrorsHandler.ResultDto getResultDto(int general,
                                                          int multipleGeneral,
                                                          int collection,
                                                          int multipleCollection) {
        return new AnnotationErrorsHandler.ResultDto(general, multipleGeneral, collection, multipleCollection);
    }

    static Stream<Arguments> generalRulesDataSet() {
        AnnotationErrorsHandler.ResultDto resultDtoSingle = getResultDto(1, 0, 0, 0);
        AnnotationErrorsHandler.ResultDto resultDtoMultiple = getResultDto(0, 1, 0, 0);
        return Stream.of(
                Arguments.of(Dto.class, "string", StringRule.class, DEFAULT, false, resultDtoSingle, getExtractorInstance()),
                Arguments.of(Dto.class, "integer", IntegerRule.class, DEFAULT, false, resultDtoSingle, getExtractorInstance()),
                Arguments.of(Dto.class, "decimal", DoubleRule.class, DEFAULT, false, resultDtoSingle, getExtractorInstance()),
                Arguments.of(Dto.class, "loong", LongRule.class, DEFAULT, false, resultDtoSingle, getExtractorInstance()),
                Arguments.of(Dto.class, "localDateTime", LocalDateTimeRule.class, DEFAULT, false, resultDtoSingle, getExtractorInstance()),
                Arguments.of(Dto.class, "stringMultipleRules", StringRule.class, GROUP_3, true, resultDtoMultiple, getExtractorInstance(GROUP_3))
        );
    }

    @ParameterizedTest
    @MethodSource("generalRulesDataSet")
    @DisplayName("General rules info extracting")
    void generalRulesInfo(Class<?> dtoClass,
                          String fieldName,
                          Class<?> rulesClass,
                          String group,
                          boolean multiple,
                          AnnotationErrorsHandler.ResultDto resultDto,
                          RulesInfoExtractor rulesInfoExtractor) {
        Field field = getField(dtoClass, fieldName);
        Optional<IRuleInfo> iRuleInfo = rulesInfoExtractor.checkAndWrapAnnotations(field, resultDto);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfo.class, iRuleInfo.get());

        RuleInfo ruleInfo = (RuleInfo) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(rulesClass)),
                () -> assertThat(ruleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(ruleInfo.isMultipleRules(), equalTo(multiple)),
                () -> assertThat(ruleInfo.getGroup(), equalTo(group))
        );
    }

    static Stream<Arguments> collectionRulesDataSet() {
        return Stream.of(
                Arguments.of(Dto.class, "listOfDates", ListRule.class, LocalDateTimeRule.class,
                        getResultDto(1, 0, 1, 0),
                        getExtractorInstance(),
                        DEFAULT,
                        false),
                Arguments.of(Dto.class, "setOfInts", SetRule.class, IntegerRule.class,
                        getResultDto(1, 0, 1, 0),
                        getExtractorInstance(),
                        DEFAULT,
                        false),
                Arguments.of(Dto.class, "listOfStringMultipleRules", ListRule.class, StringRule.class,
                        getResultDto(0, 1, 0, 1),
                        getExtractorInstance(GROUP_1),
                        GROUP_1,
                        true)
        );
    }

    @ParameterizedTest
    @MethodSource("collectionRulesDataSet")
    @DisplayName("Collection rules info extracting")
    void collectionRulesInfo(Class<?> dtoClass,
                             String fieldName,
                             Class<?> collectionRuleClass,
                             Class<?> itemRuleClass,
                             AnnotationErrorsHandler.ResultDto resultDto,
                             RulesInfoExtractor rulesInfoExtractor,
                             String group,
                             boolean multipleRules) {
        Field field = getField(dtoClass, fieldName);
        Optional<IRuleInfo> iRuleInfo = rulesInfoExtractor.checkAndWrapAnnotations(field, resultDto);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfoCollection.class, iRuleInfo.get());

        RuleInfoCollection ruleInfo = (RuleInfoCollection) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(ruleInfo.getCollectionRule().getClass(), equalTo(RuleInfo.class)),
                () -> assertThat(ruleInfo.getItemRule().getClass(), equalTo(RuleInfo.class)),
                () -> assertThat(ruleInfo.getGroup(), equalTo(group))
        );

        RuleInfo collectionRuleInfo = (RuleInfo) ruleInfo.getCollectionRule();

        assertAll(
                () -> assertThat(collectionRuleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(collectionRuleInfo.getRuleType(), equalTo(RuleType.COLLECTION)),
                () -> assertThat(collectionRuleInfo.isMultipleRules(), equalTo(multipleRules)),
                () -> assertThat(collectionRuleInfo.getGroup(), equalTo(group))
        );

        RuleInfo itemRuleInfo = (RuleInfo) ruleInfo.getItemRule();

        assertAll(
                () -> assertThat(itemRuleInfo.getRule().annotationType(), equalTo(itemRuleClass)),
                () -> assertThat(itemRuleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(itemRuleInfo.isMultipleRules(), equalTo(multipleRules)),
                () -> assertThat(itemRuleInfo.getGroup(), equalTo(group))
        );
    }

}
