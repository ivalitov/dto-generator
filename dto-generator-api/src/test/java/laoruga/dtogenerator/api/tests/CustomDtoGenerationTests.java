package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.DtoGeneratorBuilder;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientRemark;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Type Generators Tests")
public class CustomDtoGenerationTests {

    public static void baseAssertions(ClientDto dto) {
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
    public void customDtoGenerationWithDependentDto() {
        ClientDto dto = DtoGenerator.builder().build().generateDto(ClientDto.class);
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
    public void customDtoGenerationWithRemarks(ClientType clientType, DocType docType) {
        DtoGeneratorBuilder builder = DtoGenerator.builder();
        builder.addRuleRemarkForFields(ClientRemark.CLIENT_TYPE.wrap(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarkForFields(ClientRemark.DOCUMENT.wrap(docType.name()));
        }
        ClientDto dto = builder.build().generateDto(ClientDto.class);

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
    public void customDtoGenerationWithDefaultArgs(ClientType clientType, DocType docType) {
        DtoGeneratorBuilder builder = DtoGenerator.builder();
        builder.addRuleRemarkForFields(ClientRemark.CLIENT_TYPE.wrap(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarkForFields(ClientRemark.DOCUMENT.wrap(docType.name()));
        }
        ClientDto dto = builder.build().generateDto(ClientDto.class);

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
