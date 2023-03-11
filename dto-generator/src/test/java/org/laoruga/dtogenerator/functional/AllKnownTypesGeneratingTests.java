package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;
import org.laoruga.dtogenerator.constants.RulesInstance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.laoruga.dtogenerator.UtilsRoot.resetStaticConfig;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@DisplayName("All Known Types Generating Tests")
@Epic("ALL_KNOWN_TYPES_GENERATING")
@Slf4j
public class AllKnownTypesGeneratingTests {

    @NoArgsConstructor
    @Getter
    static class Dto {

        String string;
        Integer integer;
        Long aLong;
        Double aDouble;
        Boolean aBoolean;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;
        Set<Long> setOfLong;
        @NestedDtoRule
        InnerDto innerDto;

        public String getLocalDateTime() {
            return localDateTime.toString();
        }
    }

    @NoArgsConstructor
    @Getter
    static class InnerDto {

        String string;
        Integer integer;
        Long aLong;
        Double aDouble;
        Boolean aBoolean;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;
        Set<Long> setOfLong;

        public String getLocalDateTime() {
            return localDateTime.toString();
        }
    }

    @BeforeEach
    void before() {
        DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
    }

    @Test
    @DisplayName("Generation with implicit rules")
    void GenerationWithImplicitRules() {

        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getCollectionConfig(List.class).setMinSize(1);
        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getCollectionConfig(List.class).setMaxSize(1);

        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getCollectionConfig(Set.class).setMinSize(1);
        DtoGeneratorStaticConfig.getInstance().getTypeGeneratorsConfig().getCollectionConfig(Set.class).setMaxSize(1);

        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        log.info(UtilsRoot.toJson(dto));

        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minValue()))
                        .and(lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxValue()))),
                () -> assertThat(dto.getLocalDateTime(), notNullValue()),
                () -> assertThat(dto.getClientType(), notNullValue()),
                () -> assertThat(dto.getListOfString().size(), equalTo(1)),
                () -> assertThat(dto.getSetOfLong().size(), equalTo(1)),
                () -> assertThat(dto.getADouble(), notNullValue())
        );

        assertAll(
                () -> assertThat(dto.getInnerDto().getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(dto.getInnerDto().getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.getInnerDto().getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(dto.getInnerDto().getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minValue()))
                        .and(lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxValue()))),
                () -> assertThat(dto.getInnerDto().getLocalDateTime(), notNullValue()),
                () -> assertThat(dto.getInnerDto().getClientType(), notNullValue()),
                () -> assertThat(dto.getInnerDto().getListOfString().size(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getSetOfLong().size(), equalTo(1)),
                () -> assertThat(dto.getInnerDto().getADouble(), notNullValue())
        );
    }

    @AfterEach
    void after() {
        DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().setGenerateAllKnownTypes(false);
        resetStaticConfig();
    }
}
