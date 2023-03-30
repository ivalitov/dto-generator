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
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.*;

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
        @NumberRule()
        private Integer intDefaultRules;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoCustomNested {
        @NumberRule()
        private Integer intDefaultRules;
        @NestedDtoRule()
        private ClientDto clientDto;
    }

    @Getter
    @NoArgsConstructor
    static class DtoWithNestedLevels {
        @NumberRule()
        private Integer simpleInt;
        @NestedDtoRule()
        private Nested_1 nested_1;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNestedInteger;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_1 {

        @NumberRule(minInt = 1, maxInt = 2)
        private Integer oneTwo;
        @NestedDtoRule()
        private Nested_2 nested_2;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_2 {
        @NumberRule()
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

    @RepeatedTest(10)
    @DisplayName("Nested Dto Generation With Custom Rules")
    void nestedDtoWithCustomRules() {
        DtoCustomNested dto = DtoGenerator.builder(DtoCustomNested.class).build().generateDto();
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));

        // Dto generated with custom generator
        ClientDto clientDto = dto.getClientDto();

        // base assertions for client generation
        CustomDtoGenerationTests.baseAssertions(clientDto);

        ClientInfoDto clientInfo = clientDto.getClientInfo();
        if (clientInfo.getClientType() == ClientType.ORG) {
            OrgInfoDto orgInfo = ((OrgInfoDto) clientInfo);
            assertThat(orgInfo.getOrgName(), not(startsWith(ClientDto.PREFIX)));
        } else {
            PersonInfoDto personInfo = ((PersonInfoDto) clientInfo);
            assertAll(
                    () -> assertThat(personInfo.getFirstName(), not(startsWith(ClientDto.PREFIX))),
                    () -> assertThat(personInfo.getMiddleName(), not(startsWith(ClientDto.PREFIX))),
                    () -> assertThat(personInfo.getSecondName(), not(startsWith(ClientDto.PREFIX)))
            );
        }

        ClientInfoDto clientInfoWithPrefix = clientDto.getClientInfoWithPrefix();
        if (clientInfoWithPrefix.getClientType() == ClientType.ORG) {
            OrgInfoDto orgInfoWithPrefix = ((OrgInfoDto) clientInfoWithPrefix);
            assertThat(orgInfoWithPrefix.getOrgName(), startsWith(ClientDto.PREFIX));
        } else {
            PersonInfoDto personInfoWithPrefix = ((PersonInfoDto) clientInfoWithPrefix);
            assertAll(
                    () -> assertThat(personInfoWithPrefix.getFirstName(), startsWith(ClientDto.PREFIX)),
                    () -> assertThat(personInfoWithPrefix.getMiddleName(), startsWith(ClientDto.PREFIX)),
                    () -> assertThat(personInfoWithPrefix.getSecondName(), startsWith(ClientDto.PREFIX))
            );
        }
    }

    @Test
    @DisplayName("Nested Dto Remark")
    void nestedDtoRemark() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .setRuleRemark("dtoNested.intDefaultRules", RuleRemark.MIN_VALUE)
                .setRuleRemark("dtoNested.intRightBound", RuleRemark.MAX_VALUE)
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
                .setRuleRemark(RuleRemark.MIN_VALUE)
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

        @NestedDtoRule(ruleRemark = RuleRemark.MAX_VALUE)
        private Nested_3 maxValues;

        @NestedDtoRule(ruleRemark = RuleRemark.MIN_VALUE)
        private Nested_3 minValues;
    }

    static class Nested_3 {

        @NumberRule
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
                () -> assertThat(dto.notDefined.aDouble, not(equalTo(Double.MAX_VALUE))),
                () -> assertThat(dto.notDefined.integer, not(equalTo(Integer.MAX_VALUE))),

                () -> assertThat(dto.maxValues.aDouble, equalTo(Double.MAX_VALUE)),
                () -> assertThat(dto.maxValues.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.minValues.aDouble, equalTo(Double.MIN_VALUE)),
                () -> assertThat(dto.minValues.integer, equalTo(Integer.MIN_VALUE))
        );
    }

    @Test
    void nestedDtoAnnotationRemarksGeneralOverride() {
        DtoGeneratorBuilder<DtoAnnotationNestedRemarks> builder = DtoGenerator.builder(DtoAnnotationNestedRemarks.class);

        builder.setRuleRemark(RuleRemark.MAX_VALUE);

        DtoAnnotationNestedRemarks dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.notDefined.aDouble, equalTo(Double.MAX_VALUE)),
                () -> assertThat(dto.notDefined.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.maxValues.aDouble, equalTo(Double.MAX_VALUE)),
                () -> assertThat(dto.maxValues.integer, equalTo(Integer.MAX_VALUE)),

                () -> assertThat(dto.minValues.aDouble, equalTo(Double.MAX_VALUE)),
                () -> assertThat(dto.minValues.integer, equalTo(Integer.MAX_VALUE))
        );
    }

    @Test
    void nestedDtoAnnotationRemarksOverrideByField() {
        DtoGeneratorBuilder<DtoAnnotationNestedRemarks> builder = DtoGenerator.builder(DtoAnnotationNestedRemarks.class);

        builder
                .setRuleRemark("notDefined.aDouble", RuleRemark.MIN_VALUE)
                .setRuleRemark("notDefined.integer", RuleRemark.MAX_VALUE)
                .setRuleRemark("maxValues.aDouble", RuleRemark.MIN_VALUE)
                .setRuleRemark("maxValues.integer", RuleRemark.MIN_VALUE)
                .setRuleRemark("minValues.aDouble", RuleRemark.MAX_VALUE)
                .setRuleRemark("minValues.integer", RuleRemark.MAX_VALUE);

        Consumer<DtoAnnotationNestedRemarks> assertions = dto ->
                assertAll(
                        () -> assertThat(dto.notDefined.aDouble, equalTo(Double.MIN_VALUE)),
                        () -> assertThat(dto.notDefined.integer, equalTo(Integer.MAX_VALUE)),

                        () -> assertThat(dto.maxValues.aDouble, equalTo(Double.MIN_VALUE)),
                        () -> assertThat(dto.maxValues.integer, equalTo(Integer.MIN_VALUE)),

                        () -> assertThat(dto.minValues.aDouble, equalTo(Double.MAX_VALUE)),
                        () -> assertThat(dto.minValues.integer, equalTo(Integer.MAX_VALUE))
                );

        DtoAnnotationNestedRemarks dto1 = builder.build().generateDto();

        assertions.accept(dto1);

        DtoAnnotationNestedRemarks dto2 = builder.setRuleRemark(RuleRemark.MAX_VALUE).build().generateDto();

        // remarks priority check
        assertions.accept(dto2);
    }

    static class DtoWithNestedWithCustom {

        @NumberRule
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

    static class CustomTypeGenerator implements ICustomGeneratorDtoDependent<CustomType, Nested_4> {
        Supplier<Nested_4> generatedDto;

        @Override
        public CustomType generate() {
            return new CustomType(generatedDto.get().string);
        }

        @Override
        public void setDtoSupplier(Supplier<Nested_4> generatedDto) {
            this.generatedDto = generatedDto;
        }

        @Override
        public boolean isDtoReady() {
            return generatedDto.get().string != null;
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
                () -> assertThat(dto.nestedDto.customType.value, equalTo(dto.nestedDto.string))
        );
    }

}
