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
import org.laoruga.dtogenerator.config.DtoGeneratorConfig;
import org.laoruga.dtogenerator.functional.data.dtoclient.ClientType;
import org.laoruga.dtogenerator.functional.util.TestUtils;

import java.time.LocalDateTime;

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

        public String getLocalDateTime() {
            return localDateTime.toString();
        }
    }

    @BeforeAll
    static void before() {
        DtoGeneratorConfig.setGenerateAllKnownTypes(true);
    }

    @Test
    @DisplayName("Generation with implicit rules")
    void GenerationWithImplicitRules() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();
        log.info(TestUtils.toJson(dto));
    }

    @AfterAll
    static void after() {
        DtoGeneratorConfig.setGenerateAllKnownTypes(false);
    }
}
