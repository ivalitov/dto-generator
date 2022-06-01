package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static laoruga.dtogenerator.api.constants.Group.REQUIRED;
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

        @IntegerRules(group = REQUIRED)
        Integer reqInt;

        @NestedDtoRules(group = REQUIRED)
        DtoInner reqInnerDto;

        @CustomGenerator(generatorClass = MapGen.class, group = REQUIRED)
        Map<String, String> reqMap;

        @StringRules
        String defaultStr;

        @IntegerRules
        Integer defaultInt;

        @NestedDtoRules
        DtoInner defaultInnerDto;
    }

    @Getter
    @NoArgsConstructor
    static class DtoInner {

        @StringRules(group = REQUIRED)
        String reqStr;

        @IntegerRules(group = REQUIRED)
        Integer reqInt;

        @StringRules
        String defaultStr;

        @IntegerRules
        Integer defaultInt;

        @CustomGenerator(generatorClass = MapGen.class)
        Map<String, String> reqMap;
    }

    static class MapGen implements ICustomGenerator<Map<String, String>> {
        @Override
        public Map<String, String> generate() {
            return new HashMap<>();
        }
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
                () -> assertNotNull(dto.getReqInt()),
                () -> assertNotNull(dto.getReqInnerDto()),
                () -> assertNotNull(dto.getReqMap()),
                () -> assertNotNull(dto.getReqInnerDto().getReqInt()),
                () -> assertNotNull(dto.getReqInnerDto().getReqStr()),
                () -> assertNotNull(dto.getReqInnerDto().getReqMap()),
                () -> assertNull(dto.getDefaultStr()),
                () -> assertNull(dto.getDefaultInt()),
                () -> assertNull(dto.getDefaultInnerDto())
        );
    }
}
