package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.rules.CustomRule;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static laoruga.dtogenerator.api.constants.Group.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 31.05.2022
 */
@DisplayName("Fields grouping")
@Epic("FIELDS_GROUPING")
class FieldsGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @StringRule(group = REQUIRED)
        String reqStr;

        @IntegerRule(group = REQUIRED)
        Integer reqInt;

        @NestedDtoRules(group = REQUIRED)
        DtoInner reqInnerDto;

        @CustomRule(group = REQUIRED, generatorClass = MapGen.class)
        Map<String, String> reqMap;

        @StringRule
        String defaultStr;

        @IntegerRule
        Integer defaultInt;

        @NestedDtoRules
        DtoInner defaultInnerDto;

        @IntegerRule(group = GROUP_1)
        Integer firstGroupInt;

        @NestedDtoRules(group = GROUP_1)
        DtoInner firstGroupInnerDto;

        @CustomRule(group = GROUP_1, generatorClass = MapGen.class)
        Map<String, String> firstGroupMap;

        @IntegerRule(group = GROUP_2)
        Integer secondGroupInt;
    }

    @Getter
    @NoArgsConstructor
    static class DtoInner {

        @StringRule(group = REQUIRED)
        String reqStr;

        @IntegerRule(group = REQUIRED)
        Integer reqInt;

        @StringRule
        String defaultStr;

        @IntegerRule
        Integer defaultInt;

        @CustomRule(generatorClass = MapGen.class)
        Map<String, String> defaultMap;

        @IntegerRule(group = GROUP_1)
        Integer firstGroupInt;

        @CustomRule(generatorClass = MapGen.class, group = GROUP_1)
        Map<String, String> firstGroupMap;

        @IntegerRule(group = GROUP_2)
        Integer secondGroupInt;
    }

    static class MapGen implements ICustomGenerator<Map<String, String>> {
        @Override
        public Map<String, String> generate() {
            return new HashMap<>();
        }
    }

    @Test
    @DisplayName("Generating required fields only")
    void requiredGroup() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .includeGroups(REQUIRED)
                .build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getReqStr()),
                () -> assertNotNull(dto.getReqInt()),
                () -> assertNotNull(dto.getReqMap()),
                () -> assertNotNull(dto.getReqInnerDto()),
                () -> assertNotNull(dto.getReqInnerDto().getReqInt()),
                () -> assertNotNull(dto.getReqInnerDto().getReqStr()),
                () -> assertNull(dto.getReqInnerDto().getDefaultMap()),
                () -> assertNull(dto.getReqInnerDto().getDefaultInt()),
                () -> assertNull(dto.getReqInnerDto().getDefaultStr()),
                () -> assertNull(dto.getReqInnerDto().getFirstGroupMap()),
                () -> assertNull(dto.getReqInnerDto().getFirstGroupInt()),
                () -> assertNull(dto.getReqInnerDto().getSecondGroupInt()),
                () -> assertNull(dto.getDefaultStr()),
                () -> assertNull(dto.getDefaultInt()),
                () -> assertNull(dto.getDefaultInnerDto()),
                () -> assertNull(dto.getFirstGroupInt()),
                () -> assertNull(dto.getFirstGroupMap()),
                () -> assertNull(dto.getFirstGroupInnerDto()),
                () -> assertNull(dto.getSecondGroupInt())
        );
    }

    @Test
    @DisplayName("Generating GROUP_1 fields only")
    void firstGroup() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .includeGroups(GROUP_1)
                .build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNull(dto.getReqStr()),
                () -> assertNull(dto.getReqInt()),
                () -> assertNull(dto.getReqInnerDto()),
                () -> assertNull(dto.getReqMap()),
                () -> assertNull(dto.getDefaultStr()),
                () -> assertNull(dto.getDefaultInt()),
                () -> assertNull(dto.getDefaultInnerDto()),
                () -> assertNotNull(dto.getFirstGroupInt()),
                () -> assertNotNull(dto.getFirstGroupMap()),
                () -> assertNotNull(dto.getFirstGroupInnerDto()),
                () -> assertNull(dto.getSecondGroupInt())
        );
    }

    @Test
    @DisplayName("Generating required and GROUP_1 fields only")
    void requiredAndFirstGroup() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .includeGroups(REQUIRED, GROUP_1)
                .build().generateDto();

        assertNotNull(dto);

        assertAll(
                () -> assertNotNull(dto.getReqStr()),
                () -> assertNotNull(dto.getReqInt()),
                () -> assertNotNull(dto.getReqMap()),
                () -> assertNotNull(dto.getReqInnerDto()),
                () -> assertNotNull(dto.getReqInnerDto().getReqStr()),
                () -> assertNotNull(dto.getReqInnerDto().getReqInt()),
                () -> assertNull(dto.getReqInnerDto().getDefaultStr()),
                () -> assertNull(dto.getReqInnerDto().getDefaultInt()),
                () -> assertNull(dto.getReqInnerDto().getDefaultMap()),
                () -> assertNotNull(dto.getReqInnerDto().getFirstGroupInt()),
                () -> assertNotNull(dto.getReqInnerDto().getFirstGroupMap()),
                () -> assertNull(dto.getReqInnerDto().getSecondGroupInt()),
                () -> assertNull(dto.getDefaultStr()),
                () -> assertNull(dto.getDefaultInt()),
                () -> assertNull(dto.getDefaultInnerDto()),
                () -> assertNotNull(dto.getFirstGroupInt()),
                () -> assertNotNull(dto.getFirstGroupInnerDto()),
                () -> assertNotNull(dto.getFirstGroupMap()),
                () -> assertNull(dto.getSecondGroupInt())
        );
    }
}
