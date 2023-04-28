package org.laoruga.dtogenerator.rule;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.CharSet;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.laoruga.dtogenerator.constants.Group.GROUP_1;

/**
 * @author Il'dar Valitov
 * Created on 14.11.2022
 */
@DisplayName("RulesInfoExtractor tests")
@Epic("UNIT_TESTS")
@Feature("RULES_INFO_EXTRACTOR")
class RulesInfoExtractorTests {

    static class Dto {
        @StringRule(minLength = 1, maxLength = 1, chars = CharSet.NUM)
        String string;
        @IntegralRule
        int integer;
        @DecimalRule
        Double decimal;
        @IntegralRule
        long loong;
        @DateTimeRule
        LocalDateTime localDateTime;

        @CollectionRule(element = @Entry(dateTimeRule = @DateTimeRule))
        List<LocalDateTime> listOfDates;

        @CollectionRule(element = @Entry(integralRule = @IntegralRule))
        Set<Integer> setOfInts;

        @CollectionRule(
                group = GROUP_1,
                element = @Entry(stringRule = @StringRule(group = GROUP_1)))
        @CollectionRule(
                element = @Entry(stringRule = @StringRule))
        List<String> listOfStringMultipleRules;

        @StringRule(group = Group.GROUP_3)
        @StringRule(group = Group.GROUP_2)
        @StringRule
        String stringMultipleRules;
    }


    static Stream<Arguments> generalRulesDataSet() {
        return Stream.of(
                Arguments.of(Dto.class, "string", StringRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "integer", IntegralRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "decimal", DecimalRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "loong", IntegralRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "localDateTime", DateTimeRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "stringMultipleRules", StringRule.class, Group.GROUP_3, true, UtilsRoot.getExtractorInstance(Group.GROUP_3))
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
                          RulesInfoExtractor rulesInfoExtractor) {
        Field field = UtilsRoot.getField(dtoClass, fieldName);
        Optional<RuleInfo> iRuleInfo = rulesInfoExtractor.extractRulesInfo(field);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfoSimple.class, iRuleInfo.get());

        RuleInfoSimple ruleInfo = (RuleInfoSimple) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(rulesClass)),
                () -> assertThat(ruleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(ruleInfo.isMultipleRules(), equalTo(multiple)),
                () -> assertThat(ruleInfo.getGroup(), equalTo(group))
        );
    }

    static Stream<Arguments> collectionRulesDataSet() {
        return Stream.of(
                Arguments.of(Dto.class, "listOfDates", CollectionRule.class, DateTimeRule.class,
                        UtilsRoot.getExtractorInstance(),
                        Group.DEFAULT,
                        false),
                Arguments.of(Dto.class, "setOfInts", CollectionRule.class, IntegralRule.class,
                        UtilsRoot.getExtractorInstance(),
                        Group.DEFAULT,
                        false),
                Arguments.of(Dto.class, "listOfStringMultipleRules", CollectionRule.class, StringRule.class,
                        UtilsRoot.getExtractorInstance(GROUP_1),
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
                             RulesInfoExtractor rulesInfoExtractor,
                             String group,
                             boolean multipleRules) {
        Field field = UtilsRoot.getField(dtoClass, fieldName);
        Optional<RuleInfo> iRuleInfo = rulesInfoExtractor.extractRulesInfo(field);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfoList.class, iRuleInfo.get());

        RuleInfoList ruleInfo = (RuleInfoList) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(ruleInfo.getCollectionRuleInfo().getClass(), equalTo(RuleInfoSimple.class)),
                () -> assertThat(ruleInfo.getElementRuleInfo().getClass(), equalTo(RuleInfoSimple.class)),
                () -> assertThat(ruleInfo.getGroup(), equalTo(group))
        );

        RuleInfoSimple collectionRuleInfo = ruleInfo.getCollectionRuleInfo();

        assertAll(
                () -> assertThat(collectionRuleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(collectionRuleInfo.getRuleType(), equalTo(RuleType.COLLECTION)),
                () -> assertThat(collectionRuleInfo.isMultipleRules(), equalTo(multipleRules)),
                () -> assertThat(collectionRuleInfo.getGroup(), equalTo(group))
        );

        RuleInfoSimple elementRuleInfo = (RuleInfoSimple) ruleInfo.getElementRuleInfo();

        assertAll(
                () -> assertThat(elementRuleInfo.getRule().annotationType(), equalTo(itemRuleClass)),
                () -> assertThat(elementRuleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(elementRuleInfo.isMultipleRules(), equalTo(false)),
                () -> assertThat(elementRuleInfo.getGroup(), equalTo(group))
        );
    }

    static class DtoNegative3 {
        @StringRule
        @IntegralRule
        Long loong;
    }

    static class DtoNegative4 {
        @IntegralRule
        @IntegralRule
        @StringRule
        @StringRule
        Long loong;
    }

    static class DtoNegative7 {
        @StringRule
        @StringRule
        @IntegralRule
        String string;
    }

    static Stream<Arguments> unappropriatedDataSet() {
        final String DIFFERENT_TYPES = "Found @Rule annotations at least for 2 different types";
        return Stream.of(
                Arguments.of("loong", DtoNegative3.class, DIFFERENT_TYPES),
                Arguments.of("loong", DtoNegative4.class, DIFFERENT_TYPES),
                Arguments.of("string", DtoNegative7.class, DIFFERENT_TYPES)
        );
    }

    @ParameterizedTest
    @MethodSource("unappropriatedDataSet")
    @DisplayName("Unappropriated rule annotation")
    void unappropriatedRule(String fieldName, Class<?> dtoClass, String errMsgPart) {
        DtoGeneratorException dtoGeneratorException =
                assertThrows(DtoGeneratorException.class, () -> DtoGenerator.builder(dtoClass).build());

        assertThat(dtoGeneratorException.getMessage(), containsString(errMsgPart));
    }

}
