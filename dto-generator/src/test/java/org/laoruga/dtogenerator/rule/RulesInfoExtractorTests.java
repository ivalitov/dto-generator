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
        @NumberRule
        int integer;
        @DoubleRule
        Double decimal;
        @NumberRule
        long loong;
        @LocalDateTimeRule
        LocalDateTime localDateTime;

        @CollectionRule
        @LocalDateTimeRule
        List<LocalDateTime> listOfDates;
        @CollectionRule
        @NumberRule
        Set<Integer> setOfInts;

        @CollectionRule(group = GROUP_1)
        @StringRule(group = GROUP_1)
        @CollectionRule
        @StringRule
        List<String> listOfStringMultipleRules;

        @StringRule(group = Group.GROUP_3)
        @StringRule(group = Group.GROUP_2)
        @StringRule
        String stringMultipleRules;
    }


    static Stream<Arguments> generalRulesDataSet() {
        return Stream.of(
                Arguments.of(Dto.class, "string", StringRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "integer", NumberRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "decimal", DoubleRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "loong", NumberRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
                Arguments.of(Dto.class, "localDateTime", LocalDateTimeRule.class, Group.DEFAULT, false, UtilsRoot.getExtractorInstance()),
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
        Optional<IRuleInfo> iRuleInfo = rulesInfoExtractor.extractRulesInfo(field);

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
                Arguments.of(Dto.class, "listOfDates", CollectionRule.class, LocalDateTimeRule.class,
                        UtilsRoot.getExtractorInstance(),
                        Group.DEFAULT,
                        false),
                Arguments.of(Dto.class, "setOfInts", CollectionRule.class, NumberRule.class,
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
        Optional<IRuleInfo> iRuleInfo = rulesInfoExtractor.extractRulesInfo(field);

        assertTrue(iRuleInfo.isPresent());
        assertInstanceOf(RuleInfoCollection.class, iRuleInfo.get());

        RuleInfoCollection ruleInfo = (RuleInfoCollection) iRuleInfo.get();

        assertAll(
                () -> assertThat(ruleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(ruleInfo.getCollectionRule().getClass(), equalTo(RuleInfo.class)),
                () -> assertThat(ruleInfo.getElementRule().getClass(), equalTo(RuleInfo.class)),
                () -> assertThat(ruleInfo.getGroup(), equalTo(group))
        );

        RuleInfo collectionRuleInfo = (RuleInfo) ruleInfo.getCollectionRule();

        assertAll(
                () -> assertThat(collectionRuleInfo.getRule().annotationType(), equalTo(collectionRuleClass)),
                () -> assertThat(collectionRuleInfo.getRuleType(), equalTo(RuleType.COLLECTION)),
                () -> assertThat(collectionRuleInfo.isMultipleRules(), equalTo(multipleRules)),
                () -> assertThat(collectionRuleInfo.getGroup(), equalTo(group))
        );

        RuleInfo itemRuleInfo = (RuleInfo) ruleInfo.getElementRule();

        assertAll(
                () -> assertThat(itemRuleInfo.getRule().annotationType(), equalTo(itemRuleClass)),
                () -> assertThat(itemRuleInfo.getRuleType(), equalTo(RuleType.BASIC)),
                () -> assertThat(itemRuleInfo.isMultipleRules(), equalTo(multipleRules)),
                () -> assertThat(itemRuleInfo.getGroup(), equalTo(group))
        );
    }

    static class DtoNegative3 {
        @StringRule
        @NumberRule
        Long loong;
    }

    static class DtoNegative4 {
        @NumberRule
        @NumberRule
        @StringRule
        @StringRule
        Long loong;
    }

    static class DtoNegative5 {
        @CollectionRule
        List<String> list;
    }

    static class DtoNegative6 {
        @CollectionRule
        @CollectionRule(group = GROUP_1)
        List<String> list;
    }

    static class DtoNegative7 {
        @StringRule
        @StringRule
        @NumberRule
        String string;
    }

    static Stream<Arguments> unappropriatedDataSet() {
        return Stream.of(
                Arguments.of("loong", DtoNegative3.class, "Found @Rule annotations for '2' different types"),
                Arguments.of("loong", DtoNegative4.class, "Found repeatable @Rule annotations for '2' different types"),
                Arguments.of("list", DtoNegative5.class, "Missed @Rule annotation for collection element"),
                Arguments.of("list", DtoNegative6.class, "Missed @Rule annotation for collection element"),
                Arguments.of("string", DtoNegative7.class, "Found @Rule annotations for '2' different types (one repeatable)")
        );
    }

    @ParameterizedTest
    @MethodSource("unappropriatedDataSet")
    @DisplayName("Unappropriated rule annotation")
    void unappropriatedRule(String fieldName, Class<?> dtoClass, String errMsgPart) {
        DtoGenerator<?> generator = DtoGenerator.builder(dtoClass).build();
        assertThrows(DtoGeneratorException.class, generator::generateDto);
        Throwable exception = UtilsRoot.getErrorsMap(generator).get(fieldName);
        assertThat(exception.getCause().getMessage(),
                containsString(errMsgPart));
    }

}
