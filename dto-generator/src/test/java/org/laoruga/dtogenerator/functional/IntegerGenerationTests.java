package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.rule.RulesInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */

@DisplayName("Integer Generation Tests")
@Epic("INTEGER_RULES")
class IntegerGenerationTests {

    final static int minValueLeftBound = 99999999;
    final static int maxValueRightBound = 100;
    final static int minValueLeftAndRightBounds = -100;
    final static int maxValueLeftAndRightBounds = 0;
    final static int intPrimitiveVal = 999;

    @Getter
    @NoArgsConstructor
    static class DtoInteger {
        @IntegerRule()
        private Integer intDefaultRules;
        @IntegerRule()
        private int intPrimitiveDefaultRules;
        @IntegerRule(minValue = minValueLeftBound)
        private Integer intLeftBound;
        @IntegerRule(maxValue = maxValueRightBound)
        private int intRightBound;
        @IntegerRule(minValue = minValueLeftAndRightBounds, maxValue = maxValueLeftAndRightBounds)
        private int intLeftAndRightBounds;
        private int intPrimitiveDefault;
        private int intPrimitive = intPrimitiveVal;
    }

    public static void simpleIntegerGenerationAssertions(DtoInteger dto) {
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(dto.getIntLeftBound(), greaterThanOrEqualTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(maxValueRightBound))),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("SIMPLE_GENERATION")
    @DisplayName("Simple Integer Generation")
    void simpleIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).build().generateDto();
        simpleIntegerGenerationAssertions(dto);
    }

    @Test
    @Feature("ALL_FIELDS_REMARK")
    @DisplayName("MIN Values Integer Generation")
    void minIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setRuleRemark(RuleRemark.MIN_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(minValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("ALL_FIELDS_REMARK")
    @DisplayName("MAX Values Integer Generation")
    void maxIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setRuleRemark(RuleRemark.MAX_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(RulesInstance.integerRule.maxValue())),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.integerRule.maxValue())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(RulesInstance.integerRule.maxValue())),
                () -> assertThat(dto.getIntRightBound(), equalTo(maxValueRightBound)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(maxValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("EXPLICIT_FIELDS_REMARK")
    @DisplayName("Integer Generation with field remarks")
    void integerGenerationExplicitFields() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class)
                .setRuleRemark("intDefaultRules", RuleRemark.NULL_VALUE)
                .setRuleRemark("intPrimitiveDefaultRules", RuleRemark.NULL_VALUE)
                .setRuleRemark("intLeftBound", RuleRemark.MIN_VALUE)
                .setRuleRemark("intRightBound", RuleRemark.MAX_VALUE)
                .setRuleRemark("intLeftAndRightBounds", RuleRemark.RANDOM_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), nullValue()),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(0)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), equalTo(maxValueRightBound)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("EXPLICIT_FIELDS_REMARK")
    @DisplayName("Integer Generation with field remarks and all fields remarks")
    void maxIntegerGenerationExplicitFields() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class)
                .setRuleRemark(RuleRemark.MIN_VALUE)
                .setRuleRemark("intDefaultRules", RuleRemark.NULL_VALUE)
                .setRuleRemark("intLeftBound", RuleRemark.MAX_VALUE)
                .setRuleRemark("intLeftAndRightBounds", RuleRemark.RANDOM_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), nullValue()),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(RulesInstance.integerRule.maxValue())),
                () -> assertThat(dto.getIntRightBound(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

}
