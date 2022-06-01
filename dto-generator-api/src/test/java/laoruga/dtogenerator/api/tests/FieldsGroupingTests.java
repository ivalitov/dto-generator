package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;

import static laoruga.dtogenerator.api.constants.Group.REQUIRED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 31.05.2022
 */
@DisplayName("Set Type Generators Tests")
@Epic("SET_RULES")
public class FieldsGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @StringRules(group = REQUIRED)
        String reqStr;

        @StringRules
        String defaultStr;

    }

    @Test
    @DisplayName("Set Of Integers Generation (default rules params)")
    public void listOfIntegerWithDefaultRulesPrams() {
        Dto dto = DtoGenerator.builder()
                .setGroups(true, REQUIRED)
                .build().generateDto(Dto.class);

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getReqStr()),
                () -> assertNull(dto.getDefaultStr())
        );
    }
}
