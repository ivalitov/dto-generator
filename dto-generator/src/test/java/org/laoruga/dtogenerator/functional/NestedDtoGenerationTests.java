package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.functional.data.dtoclient.*;
import org.laoruga.dtogenerator.generators.RulesInstance;

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
class NestedDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegerRule()
        private Integer intDefaultRules;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoCustomNested {
        @IntegerRule()
        private Integer intDefaultRules;
        @NestedDtoRule()
        private ClientDto clientDto;
    }

    @Getter
    @NoArgsConstructor
    static class DtoWithNestedLevels {
        @IntegerRule()
        private Integer simpleInt;
        @NestedDtoRule()
        private Nested_1 nested_1;
        @NestedDtoRule()
        private IntegerGenerationTests.DtoInteger dtoNestedInteger;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_1 {

        @IntegerRule(minValue = 1, maxValue = 2)
        private Integer oneTwo;
        @NestedDtoRule()
        private Nested_2 nested_2;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_2 {
        @IntegerRule()
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
                greaterThanOrEqualTo(RulesInstance.integerRule.minValue()))
                .and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue())));
        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @RepeatedTest(10)
    @DisplayName("Nested Dto Generation With Custom Rules")
    void nestedDtoWithCustomRules() {
        DtoCustomNested dto = DtoGenerator.builder(DtoCustomNested.class).build().generateDto();
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.integerRule.minValue()))
                .and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue())));

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
                .setRuleRemark("dtoNested.intDefaultRules", BasicRuleRemark.MIN_VALUE)
                .setRuleRemark("dtoNested.intRightBound", BasicRuleRemark.MAX_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue())));

        assertThat(dto.getDtoNested().getIntDefaultRules(), equalTo(RulesInstance.integerRule.minValue()));
        assertThat(dto.getDtoNested().getIntRightBound(), equalTo(maxValueRightBound));
        assertThat(dto.getDtoNested().getIntPrimitiveDefaultRules(), both(
                greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue())));

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
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(nested_2.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(nested_2.getStringDefaultRules(), notNullValue()),
                () -> assertThat(nested_1.getOneTwo(), anyOf(equalTo(1), equalTo(2)))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

    @RepeatedTest(10)
    @DisplayName("Multilevel Nested Dto With Remarks")
    void multiLevelNestedDtoWithRemarks() {
        DtoWithNestedLevels dto = DtoGenerator.builder(DtoWithNestedLevels.class)
                .setRuleRemark(BasicRuleRemark.MIN_VALUE)
                .build().generateDto();

        assertNotNull(dto);
        Nested_1 nested_1 = dto.getNested_1();
        assertNotNull(nested_1);
        Nested_2 nested_2 = nested_1.getNested_2();
        assertNotNull(nested_2);

        assertAll(
                () -> assertThat(dto.getSimpleInt(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(nested_2.getIntDefaultRules(), equalTo(RulesInstance.integerRule.minValue())),
                () -> assertThat(nested_2.getStringDefaultRules(), emptyString()),
                () -> assertThat(nested_1.getOneTwo(), equalTo(1))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

}
