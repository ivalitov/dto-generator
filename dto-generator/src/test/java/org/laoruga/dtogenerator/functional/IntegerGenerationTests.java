package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.RulesInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */

@DisplayName("Integer Generation Tests")
@Epic("NUMBER_RULES")
class IntegerGenerationTests {

    final static int minValueLeftBound = 99999999;
    final static int maxValueRightBound = 100;
    final static int minValueLeftAndRightBounds = -100;
    final static int maxValueLeftAndRightBounds = 0;
    final static int intPrimitiveVal = 999;

    @Getter
    @NoArgsConstructor
    static class DtoInteger {
        @IntegerRule
        private Integer intDefaultRules;
        @IntegerRule
        private int intPrimitiveDefaultRules;
        @IntegerRule(minInt = minValueLeftBound)
        private Integer intLeftBound;
        @IntegerRule(maxInt = maxValueRightBound)
        private int intRightBound;
        @IntegerRule(minInt = minValueLeftAndRightBounds, maxInt = maxValueLeftAndRightBounds)
        private int intLeftAndRightBounds;
        private int intPrimitiveDefault;
        private int intPrimitive = intPrimitiveVal;
    }

    static void simpleIntegerGenerationAssertions(DtoInteger dto) {
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getIntLeftBound(), greaterThanOrEqualTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(maxValueRightBound))),
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
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setBoundary(Boundary.MIN_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(minValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("ALL_FIELDS_REMARK")
    @DisplayName("MAX Values Integer Generation")
    void maxIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder(DtoInteger.class).setBoundary(Boundary.MAX_VALUE).build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.maxInt())),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.maxInt())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(RulesInstance.NUMBER_RULE.maxInt())),
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
                .setBoundary("intDefaultRules", Boundary.NULL_VALUE)
                .setBoundary("intPrimitiveDefaultRules", Boundary.NULL_VALUE)
                .setBoundary("intLeftBound", Boundary.MIN_VALUE)
                .setBoundary("intRightBound", Boundary.MAX_VALUE)
                .setBoundary("intLeftAndRightBounds", Boundary.RANDOM_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), nullValue()),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(Integer.MIN_VALUE)),
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
                .setBoundary(Boundary.MIN_VALUE)
                .setBoundary("intDefaultRules", Boundary.NULL_VALUE)
                .setBoundary("intLeftBound", Boundary.MAX_VALUE)
                .setBoundary("intLeftAndRightBounds", Boundary.RANDOM_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), nullValue()),
                () -> assertThat(dto.getIntPrimitiveDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntLeftBound(), equalTo(RulesInstance.NUMBER_RULE.maxInt())),
                () -> assertThat(dto.getIntRightBound(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

}
