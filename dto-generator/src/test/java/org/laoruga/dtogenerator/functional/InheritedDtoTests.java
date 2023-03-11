package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.LocalDateTimeRule;
import org.laoruga.dtogenerator.api.rules.NumberRule;
import org.laoruga.dtogenerator.api.rules.StringRule;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("Inherited Dto Tests")
@Epic("INHERITED_DTO")
class InheritedDtoTests {

    @Getter
    @NoArgsConstructor
    static class Dto extends DtoSuper {
        @NumberRule()
        private Integer upperInt;
        @StringRule()
        private String upperString;
    }

    @Getter
    @NoArgsConstructor
    static class DtoSuper {
        @CollectionRule
        @StringRule
        private List<String> superList;
        @LocalDateTimeRule
        private LocalDateTime superDateTime;
    }

    @Test
    @DisplayName("Super Class Generation")
    void test() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getUpperInt()),
                () -> assertNotNull(dto.getUpperString())
        );
        assertAll(
                () -> assertNotNull(dto.getSuperList()),
                () -> assertNotNull(dto.getSuperDateTime())
        );
    }

}
