package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.MAX_VALUE;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.MIN_VALUE;
import static laoruga.dtogenerator.api.tests.IntegerGenerationTests.maxValueRightBound;
import static laoruga.dtogenerator.api.tests.IntegerGenerationTests.simpleIntegerGenerationAssertions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */

@DisplayName("Basic Type Generators Tests")
@Epic("NESTED_DTO")
public class NestedDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegerRule()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private IntegerGenerationTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoCustomNested {
        @IntegerRule()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private ClientDto clientDto;
    }

    @Getter
    @NoArgsConstructor
    static class DtoWithNestedLevels {
        @IntegerRule()
        private Integer simpleInt;
        @NestedDtoRules()
        private Nested_1 nested_1;
        @NestedDtoRules()
        private IntegerGenerationTests.DtoInteger dtoNestedInteger;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_1 {

        @IntegerRule(minValue = 1, maxValue = 2)
        private Integer oneTwo;
        @NestedDtoRules()
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
    public void nestedDtoWithIntegerRules() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));
        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @RepeatedTest(10)
    @DisplayName("Nested Dto Generation With Custom Rules")
    public void nestedDtoWithCustomRules() {
        DtoCustomNested dto = DtoGenerator.builder().build().generateDto(DtoCustomNested.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));

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
    public void nestedDtoRemark() {
        Dto dto = DtoGenerator.builder()
                .setRuleRemarkForField("dtoNested.intDefaultRules", MIN_VALUE)
                .setRuleRemarkForField("dtoNested.intRightBound", MAX_VALUE)
                .setGeneratorForField("dtoNested.intPrimitiveDefaultRules",
                        BasicGeneratorsBuilders.integerBuilder().maxValue(5).minValue(5))
                .build().generateDto(Dto.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)));

        assertThat(dto.getDtoNested().getIntDefaultRules(), equalTo(IntegerRule.DEFAULT_MIN));
        assertThat(dto.getDtoNested().getIntRightBound(), equalTo(maxValueRightBound));
        assertThat(dto.getDtoNested().getIntPrimitiveDefaultRules(), equalTo(5));

        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @Test
    @DisplayName("Multilevel Nested Dto")
    public void multiLeveNestedDto() {
        DtoWithNestedLevels dto = DtoGenerator.builder().build().generateDto(DtoWithNestedLevels.class);

        assertNotNull(dto);
        Nested_1 nested_1 = dto.getNested_1();
        assertNotNull(nested_1);
        Nested_2 nested_2 = nested_1.getNested_2();
        assertNotNull(nested_2);

        assertAll(
                () -> assertThat(dto.getSimpleInt(), both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX))),
                () -> assertThat(nested_2.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX))),
                () -> assertThat(nested_2.getStringDefaultRules(), notNullValue()),
                () -> assertThat(nested_1.getOneTwo(), anyOf(equalTo(1), equalTo(2)))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

    @RepeatedTest(10)
    @DisplayName("Multilevel Nested Dto With Remarks")
    public void multiLevelNestedDtoWithRemarks() {
        DtoWithNestedLevels dto = DtoGenerator.builder()
                .setRuleRemarkForFields(MIN_VALUE)
                .build().generateDto(DtoWithNestedLevels.class);

        assertNotNull(dto);
        Nested_1 nested_1 = dto.getNested_1();
        assertNotNull(nested_1);
        Nested_2 nested_2 = nested_1.getNested_2();
        assertNotNull(nested_2);

        assertAll(
                () -> assertThat(dto.getSimpleInt(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(nested_2.getIntDefaultRules(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(nested_2.getStringDefaultRules(), emptyString()),
                () -> assertThat(nested_1.getOneTwo(), equalTo(1))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

}
