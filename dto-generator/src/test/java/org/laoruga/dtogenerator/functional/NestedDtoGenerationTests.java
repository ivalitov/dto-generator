package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.constants.RulesInstance;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.laoruga.dtogenerator.functional.IntegerGenerationTests.maxValueRightBound;
import static org.laoruga.dtogenerator.functional.IntegerGenerationTests.simpleIntegerGenerationAssertions;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */

@DisplayName("Nested dto Generators Tests")
@Epic("NESTED_DTO")
@Slf4j
class NestedDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegralRule()
        private Integer intDefaultRules;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoWithNestedLevels {
        @IntegralRule()
        private Integer simpleInt;
        @NestedDtoRule()
        private Nested_1 nested_1;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNestedInteger;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_1 {

        @IntegralRule(minInt = 1, maxInt = 2)
        private Integer oneTwo;
        @NestedDtoRule()
        private Nested_2 nested_2;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_2 {
        @IntegralRule()
        private Integer intDefaultRules;
        @StringRule()
        private String stringDefaultRules;
    }

    @Test
    @DisplayName("Nested Dto Generation With Integer Rules")
    void nestedDtoWithIntegerRules() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));
        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @Test
    @DisplayName("Nested Dto Remark")
    void nestedDtoRemark() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .setBoundary("dtoNested.intDefaultRules", Boundary.MIN_VALUE)
                .setBoundary("dtoNested.intRightBound", Boundary.MAX_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));

        assertThat(dto.getDtoNested().getIntDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.minInt()));
        assertThat(dto.getDtoNested().getIntRightBound(), equalTo(maxValueRightBound));
        assertThat(dto.getDtoNested().getIntPrimitiveDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));

        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @Test
    @DisplayName("Multilevel Nested Dto")
    void multiLeveNestedDto() {
        DtoWithNestedLevels dto = DtoGenerator.builder(DtoWithNestedLevels.class).build().generateDto();

        assertNotNull(dto);
        Nested_1 nested_1 = dto.getNested_1();
        assertNotNull(nested_1);
        Nested_2 nested_2 = nested_1.getNested_2();
        assertNotNull(nested_2);

        assertAll(
                () -> assertThat(dto.getSimpleInt(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(nested_2.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt())).and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(nested_2.getStringDefaultRules(), notNullValue()),
                () -> assertThat(nested_1.getOneTwo(), anyOf(equalTo(1), equalTo(2)))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

    @RepeatedTest(10)
    @DisplayName("Multilevel Nested Dto With Remarks")
    void multiLevelNestedDtoWithRemarks() {
        DtoWithNestedLevels dto = DtoGenerator.builder(DtoWithNestedLevels.class)
                .setBoundary(Boundary.MIN_VALUE)
                .build().generateDto();

        assertNotNull(dto);
        Nested_1 nested_1 = dto.getNested_1();
        assertNotNull(nested_1);
        Nested_2 nested_2 = nested_1.getNested_2();
        assertNotNull(nested_2);

        assertAll(
                () -> assertThat(dto.getSimpleInt(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(nested_2.getIntDefaultRules(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(nested_2.getStringDefaultRules(), emptyString()),
                () -> assertThat(nested_1.getOneTwo(), equalTo(1))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }


    static class DtoAnnotationNestedRemarks {

        @StringRule
        private String justString;

        @NestedDtoRule
        private Nested_3 notDefined;

        @NestedDtoRule(boundary = Boundary.MAX_VALUE)
        private Nested_3 maxValues;

        @NestedDtoRule(boundary = Boundary.MIN_VALUE)
        private Nested_3 minValues;
    }

    static class Nested_3 {

        @IntegralRule
        private Integer integer;

        @DecimalRule
        private Double aDouble;
    }

    @Test
    void nestedDtoAnnotationRemarks() {
        DtoAnnotationNestedRemarks dto = DtoGenerator.builder(DtoAnnotationNestedRemarks.class)
                .build().generateDto();

        assertAll(
                // there is few possibility of false negative result
                () -> assertThat(dto.notDefined.aDouble, not(equalTo(Bounds.DOUBLE_MAX_VALUE))),
                () -> assertThat(dto.notDefined.integer, not(equalTo(Integer.MAX_VALUE))),

                () -> assertThat(dto.maxValues.aDouble, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.maxValues.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.minValues.aDouble, equalTo(Bounds.DOUBLE_MIN_VALUE)),
                () -> assertThat(dto.minValues.integer, equalTo(Integer.MIN_VALUE))
        );
    }

    @Test
    void nestedDtoAnnotationRemarksGeneralOverride() {
        DtoGeneratorBuilder<DtoAnnotationNestedRemarks> builder = DtoGenerator.builder(DtoAnnotationNestedRemarks.class);

        builder.setBoundary(Boundary.MAX_VALUE);

        DtoAnnotationNestedRemarks dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.notDefined.aDouble, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.notDefined.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.maxValues.aDouble, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.maxValues.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.minValues.aDouble, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                () -> assertThat(dto.minValues.integer, equalTo(Integer.MAX_VALUE))
        );
    }

    @Test
    void nestedDtoAnnotationRemarksOverrideByField() {
        DtoGeneratorBuilder<DtoAnnotationNestedRemarks> builder = DtoGenerator.builder(DtoAnnotationNestedRemarks.class);

        builder
                .setBoundary("notDefined.aDouble", Boundary.MIN_VALUE)
                .setBoundary("notDefined.integer", Boundary.MAX_VALUE)
                .setBoundary("maxValues.aDouble", Boundary.MIN_VALUE)
                .setBoundary("maxValues.integer", Boundary.MIN_VALUE)
                .setBoundary("minValues.aDouble", Boundary.MAX_VALUE)
                .setBoundary("minValues.integer", Boundary.MAX_VALUE);

        Consumer<DtoAnnotationNestedRemarks> assertions = dto ->
                assertAll(
                        () -> assertThat(dto.notDefined.aDouble, equalTo(Bounds.DOUBLE_MIN_VALUE)),
                        () -> assertThat(dto.notDefined.integer, equalTo(Integer.MAX_VALUE)),

                        () -> assertThat(dto.maxValues.aDouble, equalTo(Bounds.DOUBLE_MIN_VALUE)),
                        () -> assertThat(dto.maxValues.integer, equalTo(Integer.MIN_VALUE)),

                        () -> assertThat(dto.minValues.aDouble, equalTo(Bounds.DOUBLE_MAX_VALUE)),
                        () -> assertThat(dto.minValues.integer, equalTo(Integer.MAX_VALUE))
                );

        DtoAnnotationNestedRemarks dto1 = builder.build().generateDto();

        assertions.accept(dto1);

        DtoAnnotationNestedRemarks dto2 = builder.setBoundary(Boundary.MAX_VALUE).build().generateDto();

        // remarks priority check
        assertions.accept(dto2);
    }

    static class DtoWithNestedWithCustom {

        @IntegralRule
        private Integer integer;

        @NestedDtoRule
        private Nested_4 nestedDto;
    }

    static class Nested_4 {

        @CustomRule(generatorClass = CustomTypeGenerator.class)
        private CustomType customType;

        @StringRule(words = "LUDWIG")
        private String string;
    }

    @AllArgsConstructor
    static class CustomType {
        String value;
    }

    static class CustomTypeGenerator implements CustomGeneratorDtoDependent<CustomType, Object> {
        Supplier<Object> generatedDtoSupplier;
        Object generatedDto;

        @Override
        public CustomType generate() {
            String requiredString;
            if (generatedDto.getClass() == Nested_4.class) {
                requiredString = ((Nested_4) generatedDto).string;
            } else if (generatedDto.getClass() == DtoWithNestedWithCustom.class) {
                requiredString = String.valueOf(((DtoWithNestedWithCustom) generatedDto).integer);
            } else {
                throw new IllegalStateException("Unknown root dto type: '" + generatedDto.getClass() + "'");
            }
            return new CustomType(requiredString);
        }

        @Override
        public void setDtoSupplier(Supplier<Object> generatedDto) {
            this.generatedDtoSupplier = generatedDto;
        }

        @Override
        public boolean isDtoReady() {
            if (generatedDto == null) {
                generatedDto = generatedDtoSupplier.get();
            }
            if (generatedDto.getClass() == Nested_4.class) {
                return ((Nested_4) generatedDto).string != null;
            } else if (generatedDto.getClass() == DtoWithNestedWithCustom.class) {
                return ((DtoWithNestedWithCustom) generatedDto).integer != null;
            } else {
                throw new IllegalStateException("Unknown root dto type: '" + generatedDto.getClass() + "'");
            }
        }

    }

    @Test
    void nestedDtoWithCustomDtoDependentRules() {
        DtoGeneratorBuilder<DtoWithNestedWithCustom> builder =
                DtoGenerator.builder(DtoWithNestedWithCustom.class);

        DtoWithNestedWithCustom dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.integer, notNullValue()),
                () -> assertThat(dto.nestedDto.string, notNullValue()),
                () -> assertThat(dto.nestedDto.customType.value, equalTo(String.valueOf(dto.integer)))
        );
    }

}
