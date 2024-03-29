package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.rules.NestedDtoRule;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.ClientType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.laoruga.dtogenerator.Constants.RESTORE_STATIC_CONFIG;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@DisplayName("All Known Types Generating Tests")
@Epic("ALL_KNOWN_TYPES_GENERATING")
@Slf4j
@ExtendWith(Extensions.RestoreStaticConfig.class)
class AllKnownTypesGeneratingTests {

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
        NestedDto nestedDto;

        // unknown type
        Date date;
    }

    static class NestedDto {

        String string;
        Integer integer;
        Long aLong;
        Double aDouble;
        Boolean aBoolean;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;
        Set<Long> setOfLong;

        // unknown type
        Date date;
    }

    @BeforeEach
    void before() {
        DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
    }

    @Test
    @Tag(RESTORE_STATIC_CONFIG)
    @DisplayName("Generation with implicit rules")
    void GenerationWithImplicitRules() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        TypeGeneratorsConfigSupplier staticConfig = builder.getStaticConfig().getTypeGeneratorsConfig();

        staticConfig.getCollectionConfig(List.class).setMinSize(2);
        staticConfig.getCollectionConfig(List.class).setMaxSize(2);

        staticConfig.getCollectionConfig(Set.class).setMinSize(1);
        staticConfig.getCollectionConfig(Set.class).setMaxSize(1);

        Dto dto = builder.build().generateDto();

        assertNotNull(dto);

        assertAll(
                () -> assertThat(dto.string.length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(dto.integer, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(dto.aLong, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(dto.aDouble, both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minDouble()))
                        .and(lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxDouble()))),
                () -> assertThat(dto.localDateTime, notNullValue()),
                () -> assertThat(dto.clientType, notNullValue()),
                () -> assertThat(dto.listOfString.size(), equalTo(2)),
                () -> assertThat(dto.setOfLong.size(), equalTo(1)),
                () -> assertThat(dto.aDouble, notNullValue()),
                () -> assertThat(dto.aBoolean, notNullValue()),
                () -> assertThat(dto.date, nullValue())
        );

        NestedDto nestedDto = dto.nestedDto;

        assertAll(
                () -> assertThat(nestedDto.string.length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(nestedDto.integer, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(nestedDto.aLong, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(nestedDto.aDouble, both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minDouble()))
                        .and(lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxDouble()))),
                () -> assertThat(nestedDto.localDateTime, notNullValue()),
                () -> assertThat(nestedDto.clientType, notNullValue()),
                () -> assertThat(nestedDto.listOfString.size(), equalTo(2)),
                () -> assertThat(nestedDto.setOfLong.size(), equalTo(1)),
                () -> assertThat(nestedDto.aBoolean, notNullValue()),
                () -> assertThat(nestedDto.date, nullValue())

        );
    }

}
