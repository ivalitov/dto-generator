package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import laoruga.dtogenerator.api.markup.rules.LocalDateTimeRules;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static laoruga.dtogenerator.api.constants.CharSet.NUM;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("STRING_RULES")
public class StringGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto extends DtoSuper {
        @StringRules(mask = "+89 (***) ***-**-**", charset = NUM)
        private String upperString;
    }

    @Getter
    @NoArgsConstructor
    static class DtoSuper {
        @ListRules
        @StringRules
        private List<String> superList;
        @LocalDateTimeRules
        private LocalDateTime superDateTime;
    }

    @RepeatedTest(1000)
    @DisplayName("Super Class Generation")
    public void mask() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertAll(
                () -> assertThat(dto.getUpperString(), hasLength(19)),
                () -> assertThat(dto.getUpperString(), matchesRegex("[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}"))
        );
        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getUpperString())
        );
    }

}
