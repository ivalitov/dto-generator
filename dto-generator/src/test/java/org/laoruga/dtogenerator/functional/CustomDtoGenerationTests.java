package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.functional.data.customgenerator.RemarkNonArgs;
import org.laoruga.dtogenerator.functional.data.customgenerator.RemarkUniversal;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.*;

import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.laoruga.dtogenerator.functional.data.customgenerator.ClientRemark.CLIENT_TYPE;
import static org.laoruga.dtogenerator.functional.data.customgenerator.ClientRemark.DOCUMENT;

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
        builder.addRuleRemarksCustom("clientInfo", CLIENT_TYPE.setArgs(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarksCustom(DOCUMENT.setArgs(docType.name()));
        }
        ClientDto dto = builder.build().generateDto();

        baseAssertions(dto);
        assertEquals(clientType, dto.getClientInfo().getClientType());

        if (clientType == ClientType.ORG) {
            assertEquals(OrgInfoDto.class, dto.getClientInfo().getClass());
        } else {
            assertEquals(PersonInfoDto.class, dto.getClientInfo().getClass());
            assertEquals(docType, ((PersonInfoDto) dto.getClientInfo()).getDocument().getType());
        }
    }

    @ParameterizedTest
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Dto Generation With Args")
    @MethodSource("customDtoGenerationWithRemarksTestData")
    void customDtoGenerationWithDefaultArgs(ClientType clientType, DocType docType) {
        DtoGeneratorBuilder<ClientDto> builder = DtoGenerator.builder(ClientDto.class);
        builder.addRuleRemarksCustom(CLIENT_TYPE.setArgs(clientType.name()));
        if (docType != null) {
            builder.addRuleRemarksCustom(DOCUMENT.setArgs(docType.name()));
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

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Type Generator Remarkable With Args (remark without args))")
    void customTypeGeneratorRemarkableWithArgsAndRemarkWithoutArgs() {
        ClientDto dto = DtoGenerator.builder(ClientDto.class)
                .addRuleRemarksCustom("clientInfoWithPrefix", RemarkNonArgs.NULL_VALUE)
                .build().generateDto();
        assertNotNull(dto);
        baseAssertions(dto);

        assertAll(
                () -> assertNull(dto.getClientInfoWithPrefix()),
                () -> assertNotNull(dto.getClientInfo())
        );

        assertAll(
                () -> assertNotNull(dto.getClientInfo().getClientType()),
                () -> assertNotNull(dto.getClientInfo().getId())
        );
    }

    @Getter
    @NoArgsConstructor
    static class Dto {
        String someString;
        @CustomRule(generatorClass = FooGenerator.class)
        Foo foo;

        @CustomRule(generatorClass = FooGenerator.class)
        Foo bar;
    }

    static class Foo {
    }

    static class FooGenerator implements ICustomGeneratorRemarkable<Foo> {

        Set<ICustomRuleRemark> ruleRemarks;

        @Override
        public Foo generate() {
            if (ruleRemarks.contains(RemarkNonArgs.NULL_VALUE) ||
                    ruleRemarks.contains(RemarkUniversal.NULL_VALUE)) {
                return null;
            }
            return new Foo();
        }

        @Override
        public void setRuleRemarks(Set<ICustomRuleRemark> ruleRemarks) {
            this.ruleRemarks = ruleRemarks;
        }
    }

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Custom Type Generator Remarkable Without Args (remark without args))")
    void customTypeGeneratorWithoutArgsAndRemarkWithoutArgs() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .addRuleRemarksCustom("foo", RemarkNonArgs.NULL_VALUE)
                .build().generateDto();

        assertNotNull(dto);
        assertNull(dto.getFoo());

        Dto dto2 = DtoGenerator.builder(Dto.class)
                .build().generateDto();

        assertNotNull(dto);
        assertNotNull(dto2.getFoo());
    }

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Remark Without Generator Class Specified (Generator with NO args remarks)")
    void remarkWithoutGeneratorClassSpecifiedGeneratorWithNoArgsRemarks() {

        // health check

        Dto dto0 = DtoGenerator.builder(Dto.class)
                .build().generateDto();

        assertNotNull(dto0.getFoo());


        // field not specified

        Dto dto1 = DtoGenerator.builder(Dto.class)
                .addRuleRemarksCustom(RemarkUniversal.NULL_VALUE)
                .build().generateDto();

        assertNotNull(dto1);
        assertAll(
                () -> assertNull(dto1.getFoo()),
                () -> assertNull(dto1.getBar())
        );

        // field specified

        Dto dto2 = DtoGenerator.builder(Dto.class)
                .addRuleRemarksCustom("foo", RemarkUniversal.NULL_VALUE)
                .build().generateDto();

        assertNotNull(dto2);
        assertAll(
                () -> assertNull(dto2.getFoo()),
                () -> assertNotNull(dto2.getBar())
        );

    }

    @Test
    @Feature("CUSTOM_RULES")
    @DisplayName("Remark Without Generator Class Specified (Generator with ARGS remarks)")
    void remarkWithoutGeneratorClassSpecifiedGeneratorWithArgsRemarks() {

        // field not specified

        ClientDto dto = DtoGenerator.builder(ClientDto.class)
                .addRuleRemarksCustom(RemarkUniversal.NULL_VALUE)
                .build().generateDto();
        assertNotNull(dto);

        assertAll(
                () -> assertNull(dto.getClientInfoWithPrefix()),
                () -> assertNull(dto.getClientInfo())
        );

        // field specified

        ClientDto dto2 = DtoGenerator.builder(ClientDto.class)
                .addRuleRemarksCustom("clientInfo", RemarkUniversal.NULL_VALUE)
                .build().generateDto();
        assertNotNull(dto2);

        assertAll(
                () -> assertNotNull(dto2.getClientInfoWithPrefix()),
                () -> assertNull(dto2.getClientInfo())
        );

    }

}
