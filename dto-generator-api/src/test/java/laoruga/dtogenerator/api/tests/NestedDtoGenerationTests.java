package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.tests.BasitTypeGeneratorsTests.simpleIntegerGenerationAssertions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Basic Type Generators Tests")
public class NestedDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
        @IntegerRules()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private BasitTypeGeneratorsTests.DtoInteger dtoNested;
    }

    @Getter
    @NoArgsConstructor
    static class DtoCustomNested {
        @IntegerRules()
        private Integer intDefaultRules;
        @NestedDtoRules()
        private ClientDto clientDto;
    }

    @Test
    @Feature("NESTED_DTO")
    @DisplayName("Nested Dto Generation With Integer Rules")
    public void nestedDtoWithIntegerRules() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertNotNull(dto);
        assertThat(dto.getIntDefaultRules(), both(
                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
        simpleIntegerGenerationAssertions(dto.getDtoNested());

    }

    @RepeatedTest(50)
    @Feature("NESTED_DTO")
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

}
