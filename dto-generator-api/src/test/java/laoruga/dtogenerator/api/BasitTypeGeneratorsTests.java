package laoruga.dtogenerator.api;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.MAX_VALUE;
import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.MIN_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Basic Type Generators Tests")
public class BasitTypeGeneratorsTests {

    final static int minValueLeftBound = 99999999;
    final static int maxValueRightBound = 100;
    final static int minValueLeftAndRightBounds = -100;
    final static int maxValueLeftAndRightBounds = 0;
    final static int intPrimitiveVal = 999;

    @Getter
    @NoArgsConstructor
    static class DtoInteger {
        @IntegerRules()
        private Integer intDefaultRules;
        @IntegerRules(minValue = minValueLeftBound)
        private Integer intLeftBound;
        @IntegerRules(maxValue = maxValueRightBound)
        private int intRightBound;
        @IntegerRules(minValue = minValueLeftAndRightBounds, maxValue = maxValueLeftAndRightBounds)
        private int intLeftAndRightBounds;
        private int intPrimitiveDefault;
        private int intPrimitive = intPrimitiveVal;
    }

    public static void simpleIntegerGenerationAssertions(DtoInteger dto){
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX))),
                () -> assertThat(dto.getIntLeftBound(), greaterThanOrEqualTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), both(
                        greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(maxValueRightBound))),
                () -> assertThat(dto.getIntLeftAndRightBounds(), both(
                        greaterThanOrEqualTo(minValueLeftAndRightBounds)).and(lessThanOrEqualTo(maxValueLeftAndRightBounds))),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("INTEGER_RULES")
    @DisplayName("Simple Integer Generation")
    public void simpleIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder().build().generateDto(DtoInteger.class);
        simpleIntegerGenerationAssertions(dto);
    }

    @Test
    @Feature("INTEGER_RULES")
    @DisplayName("MIN Values Integer Generation")
    public void minIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder().setRuleRemark(MIN_VALUE).build().generateDto(DtoInteger.class);
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(IntegerRules.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(minValueLeftBound)),
                () -> assertThat(dto.getIntRightBound(), equalTo(IntegerRules.DEFAULT_MIN)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(minValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

    @Test
    @Feature("INTEGER_RULES")
    @DisplayName("MAX Values Integer Generation")
    public void maxIntegerGeneration() {
        DtoInteger dto = DtoGenerator.builder().setRuleRemark(MAX_VALUE).build().generateDto(DtoInteger.class);
        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getIntDefaultRules(), equalTo(IntegerRules.DEFAULT_MAX)),
                () -> assertThat(dto.getIntLeftBound(), equalTo(IntegerRules.DEFAULT_MAX)),
                () -> assertThat(dto.getIntRightBound(), equalTo(maxValueRightBound)),
                () -> assertThat(dto.getIntLeftAndRightBounds(), equalTo(maxValueLeftAndRightBounds)),
                () -> assertThat(dto.getIntPrimitiveDefault(), equalTo(0)),
                () -> assertThat(dto.getIntPrimitive(), equalTo(intPrimitiveVal))
        );
    }

}
