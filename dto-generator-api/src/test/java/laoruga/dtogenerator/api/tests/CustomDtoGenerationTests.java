package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.RemarkableDtoGenerator;
import laoruga.dtogenerator.api.RemarkableDtoGeneratorBuilder;
import laoruga.dtogenerator.api.tests.data.customgenerator.ClientRemark;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Type Generators Tests")
public class CustomDtoGenerationTests {

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Smoke Custom Dto Generation")
    public void smokeCustomDtoGeneration() {
        ClientDto dto = DtoGenerator.builder().build().generateDto(ClientDto.class);
        assertNotNull(dto);
        baseAssertions(dto);
    }

    private void baseAssertions(ClientDto dto) {
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

    static Stream<Arguments> customDtoGenerationWithRemarksSource(){
        return Stream.of(
                Arguments.arguments(ClientType.LEGAL_PERSON, DocType.PASSPORT),
                Arguments.arguments(ClientType.PERSON, DocType.DRIVER_LICENCE),
                Arguments.arguments(ClientType.ORG, null)
        );
    }

    @ParameterizedTest
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Dto Generation With Remarks")
    @MethodSource("customDtoGenerationWithRemarksSource")
    public void customDtoGenerationWithRemarks(ClientType clientType, DocType docType) {
        RemarkableDtoGeneratorBuilder builder = RemarkableDtoGenerator.builder();
        builder.addExtendedRuleRemarks(ClientRemark.CLIENT_TYPE.wrap(clientType.name()));
        if (docType != null) {
            builder.addExtendedRuleRemarks(ClientRemark.DOCUMENT.wrap(docType.name()));
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

}
