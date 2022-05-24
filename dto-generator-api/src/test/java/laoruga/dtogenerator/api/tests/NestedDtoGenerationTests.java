package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.MAX_VALUE;
import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.MIN_VALUE;
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
        @IntegerRules()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private IntegerGenerationTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoCustomNested {
        @IntegerRules()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private ClientDto clientDto;
    }

    @Getter
    @NoArgsConstructor
    static class DtoWithNestedLevels {
        @IntegerRules()
        private Integer simpleInt;
        @NestedDtoRules()
        private Nested_1 nested_1;
        @NestedDtoRules()
        private IntegerGenerationTests.DtoInteger dtoNestedInteger;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_1 {

        @IntegerRules(minValue = 1, maxValue = 2)
        private Integer oneTwo;
        @NestedDtoRules()
        private Nested_2 nested_2;
    }

    @Getter
    @NoArgsConstructor
    static class Nested_2 {
        @IntegerRules()
        private Integer intDefaultRules;
        @StringRules()
        private String stringDefaultRules;
    }

    @Test
    @DisplayName("Nested Dto Generation With Integer Rules")
    public void nestedDtoWithIntegerRules() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        simpleIntegerGenerationAssertions(dto.getDtoNested());
    }

    @RepeatedTest(10)
    @DisplayName("Nested Dto Generation With Custom Rules")
    public void nestedDtoWithCustomRules() {
        DtoCustomNested dto = DtoGenerator.builder().build().generateDto(DtoCustomNested.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));

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
                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));

        assertThat(dto.getDtoNested().getIntDefaultRules(), equalTo(IntegerRules.DEFAULT_MIN));
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
                        greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX))),
                () -> assertThat(nested_2.getIntDefaultRules(), both(
                        greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX))),
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
                () -> assertThat(dto.getSimpleInt(), equalTo(IntegerRules.DEFAULT_MIN)),
                () -> assertThat(nested_2.getIntDefaultRules(), equalTo(IntegerRules.DEFAULT_MIN)),
                () -> assertThat(nested_2.getStringDefaultRules(), emptyString()),
                () -> assertThat(nested_1.getOneTwo(), equalTo(1))
        );

        simpleIntegerGenerationAssertions(dto.getDtoNestedInteger());
    }

}
