package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.DoubleRule;
import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.api.rules.LongRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.functional.data.dtoclient.ClientType;
import org.laoruga.dtogenerator.functional.util.TestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@DisplayName("Implicit rules")
@Epic("IMPLICIT_RULES")
@Slf4j
public class ImplicitRulesTests {

    @NoArgsConstructor
    @Getter
    static class Dto {

        String string;
        Integer integer;
        Long aLong;
        Double aDouble;
        LocalDateTime localDateTime;
        ClientType clientType;
        List<String> listOfString;

        public String getLocalDateTime() {
            return localDateTime.toString();
        }
    }

    @BeforeAll
    static void before() {
        DtoGeneratorStaticConfig.getInstance().setGenerateAllKnownTypes(true);
    }

    @Test
    @DisplayName("Generation with implicit rules")
    void GenerationWithImplicitRules() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        log.info(TestUtils.toJson(dto));

        assertNotNull(dto);
        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(StringRule.DEFAULT_MIN_SYMBOLS_NUMBER))
                        .and(lessThanOrEqualTo(StringRule.DEFAULT_MAX_SYMBOLS_NUMBER))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN))
                        .and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(LongRule.DEFAULT_MIN))
                        .and(lessThanOrEqualTo(LongRule.DEFAULT_MAX))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(DoubleRule.DEFAULT_MIN))
                        .and(lessThanOrEqualTo(DoubleRule.DEFAULT_MAX))),
                () -> assertThat(dto.getLocalDateTime(), notNullValue()),
                () -> assertThat(dto.getClientType(), notNullValue())

        );
    }

    @AfterAll
    static void after() {
        DtoGeneratorStaticConfig.getInstance().setGenerateAllKnownTypes(false);
    }
}
