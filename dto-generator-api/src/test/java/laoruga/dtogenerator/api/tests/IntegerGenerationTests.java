package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.*;
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
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX))),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX))),
                () -> assertThat(dto.getIntLeftBound(), greaterThanOrEqualTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(maxValueRightBound))),
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
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setRuleRemarkForFields(MIN_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(minValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("ALL_FIELDS_REMARK")
    @DisplayName("MAX Values Integer Generation")
    void maxIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setRuleRemarkForFields(MAX_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(IntegerRule.DEFAULT_MAX)),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(IntegerRule.DEFAULT_MAX)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(IntegerRule.DEFAULT_MAX)),
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
                .setRuleRemarkForField("intDefaultRules", NULL_VALUE)
                .setRuleRemarkForField("intPrimitiveDefaultRules", NULL_VALUE)
                .setRuleRemarkForField("intLeftBound", MIN_VALUE)
                .setRuleRemarkForField("intRightBound", MAX_VALUE)
                .setRuleRemarkForField("intLeftAndRightBounds", RANDOM_VALUE)
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
                .setRuleRemarkForFields(MIN_VALUE)
                .setRuleRemarkForField("intDefaultRules", NULL_VALUE)
                .setRuleRemarkForField("intLeftBound", MAX_VALUE)
                .setRuleRemarkForField("intLeftAndRightBounds", RANDOM_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), nullValue()),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(IntegerRule.DEFAULT_MAX)),
                () -> assertThat(dto.getIntRightBound(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

}
