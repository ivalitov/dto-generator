package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.markup.rules.LocalDateTimeRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
public class InheritedDto {

    @Getter
    @NoArgsConstructor
    static class Dto extends DtoSuper {
        @IntegerRule()
        private Integer upperInt;
        @StringRule()
        private String upperString;
    }

    @Getter
    @NoArgsConstructor
    static class DtoSuper {
        @ListRules
        @StringRule
        private List<String> superList;
        @LocalDateTimeRules
        private LocalDateTime superDateTime;
    }

    @Test
    @DisplayName("Super Class Generation")
    public void test() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);

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
