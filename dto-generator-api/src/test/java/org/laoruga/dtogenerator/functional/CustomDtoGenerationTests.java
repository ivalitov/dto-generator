package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.functional.data.customgenerator.ClientRemark;
import org.laoruga.dtogenerator.functional.data.dtoclient.*;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 04.05.2022
 */

@DisplayName("Custom Type Generators Tests")
class CustomDtoGenerationTests {

    static void baseAssertions(ClientDto dto) {
        ClientInfoDto clientInfo = dto.getClientInfo();
        String stringRequiredForClient = dto.getStringRequiredForClient();
        assertAll(
                () -> assertNotNull(clientInfo),
                () -> assertNotNull(stringRequiredForClient)
        );

        assertAll(
                () -> assertEquals(stringRequiredForClient, clientInfo.getId()),
                () -> assertNotNull(clientInfo.getClientType())
        );
    }

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Dto Generation With Dependent Dto")
    void customDtoGenerationWithDependentDto() {
        ClientDto dto = DtoGenerator.builder(ClientDto.class).build().generateDto();
        assertNotNull(dto);
        baseAssertions(dto);
    }

    static Stream<Arguments> customDtoGenerationWithRemarksTestData() {
        return Stream.of(
                Arguments.arguments(ClientType.LEGAL_PERSON, DocType.PASSPORT),
                Arguments.arguments(ClientType.PERSON, DocType.DRIVER_LICENCE),
                Arguments.arguments(ClientType.ORG, null)
        );
    }

    @ParameterizedTest
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Dto Generation With Remarks")
    @MethodSource("customDtoGenerationWithRemarksTestData")
    void customDtoGenerationWithRemarks(ClientType clientType, DocType docType) {
        DtoGeneratorBuilder<ClientDto> builder = DtoGenerator.builder(ClientDto.class);
        builder.addRuleRemarkForFields(ClientRemark.CLIENT_TYPE.wrap(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarkForFields(ClientRemark.DOCUMENT.wrap(docType.name()));
        }
        ClientDto dto = builder.build().generateDto();

        baseAssertions(dto);
        assertEquals(clientType, dto.getClientInfo().getClientType());

        if (clientType == ClientType.ORG) {
            assertEquals(dto.getClientInfo().getClass(), OrgInfoDto.class);
        } else {
            assertEquals(dto.getClientInfo().getClass(), PersonInfoDto.class);
            assertEquals(((PersonInfoDto) dto.getClientInfo()).getDocument().getType(), docType);
        }
    }

    @ParameterizedTest
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Dto Generation With Args")
    @MethodSource("customDtoGenerationWithRemarksTestData")
    void customDtoGenerationWithDefaultArgs(ClientType clientType, DocType docType) {
        DtoGeneratorBuilder<ClientDto> builder = DtoGenerator.builder(ClientDto.class);
        builder.addRuleRemarkForFields(ClientRemark.CLIENT_TYPE.wrap(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarkForFields(ClientRemark.DOCUMENT.wrap(docType.name()));
        }
        ClientDto dto = builder.build().generateDto();

        baseAssertions(dto);
        assertEquals(clientType, dto.getClientInfo().getClientType());

        if (clientType == ClientType.ORG) {
            OrgInfoDto orgInfo = ((OrgInfoDto) dto.getClientInfo());
            OrgInfoDto orgInfoWithPrefix = ((OrgInfoDto) dto.getClientInfoWithPrefix());
            assertAll(
                    () -> assertThat(orgInfo.getOrgName(), not(startsWith(ClientDto.PREFIX))),
                    () -> assertThat(orgInfoWithPrefix.getOrgName(), startsWith(ClientDto.PREFIX))
            );

        } else {
            PersonInfoDto personInfo = ((PersonInfoDto) dto.getClientInfo());
            PersonInfoDto personInfoWithPrefix = ((PersonInfoDto) dto.getClientInfoWithPrefix());
            assertAll(
                    () -> assertThat(personInfo.getFirstName(), not(startsWith(ClientDto.PREFIX))),
                    () -> assertThat(personInfo.getMiddleName(), not(startsWith(ClientDto.PREFIX))),
                    () -> assertThat(personInfo.getSecondName(), not(startsWith(ClientDto.PREFIX))),

                    () -> assertThat(personInfoWithPrefix.getFirstName(), startsWith(ClientDto.PREFIX)),
                    () -> assertThat(personInfoWithPrefix.getMiddleName(), startsWith(ClientDto.PREFIX)),
                    () -> assertThat(personInfoWithPrefix.getSecondName(), startsWith(ClientDto.PREFIX))
            );
        }
    }

}
