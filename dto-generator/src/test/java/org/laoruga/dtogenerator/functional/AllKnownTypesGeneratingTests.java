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
import org.laoruga.dtogenerator.generator.config.dto.DecimalConfig;
import org.laoruga.dtogenerator.generator.config.dto.IntegralConfig;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
        BigInteger bigInteger;
        AtomicLong atomicLong;
        Long aLong;
        Double aDouble;
        BigDecimal bigDecimal;
        Boolean aBoolean;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;
        Set<Long> setOfLong;
        @NestedDtoRule
        InnerDto innerDto;

        // unknown type
        Date date;
    }

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

        InnerDto innerDto = dto.innerDto;

        assertAll(
                () -> assertThat(innerDto.string.length(), both(
                        greaterThanOrEqualTo(RulesInstance.STRING_RULE.minLength()))
                        .and(lessThanOrEqualTo(RulesInstance.STRING_RULE.maxLength()))),
                () -> assertThat(innerDto.integer, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt()))),
                () -> assertThat(innerDto.aLong, both(
                        greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minLong()))
                        .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxLong()))),
                () -> assertThat(innerDto.aDouble, both(
                        greaterThanOrEqualTo(RulesInstance.DECIMAL_RULE.minDouble()))
                        .and(lessThanOrEqualTo(RulesInstance.DECIMAL_RULE.maxDouble()))),
                () -> assertThat(innerDto.localDateTime, notNullValue()),
                () -> assertThat(innerDto.clientType, notNullValue()),
                () -> assertThat(innerDto.listOfString.size(), equalTo(2)),
                () -> assertThat(innerDto.setOfLong.size(), equalTo(1)),
                () -> assertThat(innerDto.aDouble, notNullValue()),
                () -> assertThat(innerDto.aBoolean, notNullValue()),
                () -> assertThat(innerDto.date, nullValue())

        );
    }

    @Test
    void configOverriddenForNumberTypes() {

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class).generateKnownTypes();

        builder
                .setGeneratorConfig("integer", IntegralConfig.builder().maxValue(7).minValue(7).build())
                .setGeneratorConfig("aDouble", DecimalConfig.builder().maxValue(8D).minValue(8D).build())
                .setGeneratorConfig("atomicLong", IntegralConfig.builder().maxValue(9L).minValue(9L).build())
                .setGeneratorConfig("bigInteger", IntegralConfig.builder()
                        .maxValue(new BigInteger("10"))
                        .minValue(new BigInteger("10")).build())
                .setGeneratorConfig("bigDecimal", DecimalConfig.builder()
                        .maxValue(new BigDecimal("11"))
                        .minValue(new BigDecimal("11")).build());

        Dto dto = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto.integer, equalTo(7)),
                () -> assertThat(dto.aDouble, equalTo(8D)),
                () -> assertThat(dto.atomicLong.get(), equalTo(9L)),
                () -> assertThat(dto.bigInteger, equalTo(new BigInteger("10"))),
                () -> assertThat(dto.bigDecimal, equalTo(new BigDecimal("11")))
        );

    }

}
