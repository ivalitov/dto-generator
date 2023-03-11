package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.UtilsRoot;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.api.rules.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.laoruga.dtogenerator.constants.Group.*;

/**
 * @author Il'dar Valitov
 * Created on 31.05.2022
 */
@DisplayName("Fields grouping")
@Epic("FIELDS_GROUPING")
@Slf4j
class FieldsGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        // this annotation do not mean anything
        @StringRule(group = REQUIRED)
        String reqStr;

        @NumberRule(group = REQUIRED)
        Integer reqInt;

        @NestedDtoRule(group = REQUIRED)
        DtoInner reqInnerDto;

        @CustomRule(group = REQUIRED, generatorClass = MapGen.class)
        Map<String, String> reqMap;

        @StringRule
        String defaultStr;

        @NumberRule
        Integer defaultInt;

        @NestedDtoRule
        DtoInner defaultInnerDto;

        @NumberRule(group = GROUP_1)
        Integer firstGroupInt;

        @NestedDtoRule(group = GROUP_1)
        DtoInner firstGroupInnerDto;

        @CustomRule(group = GROUP_1, generatorClass = MapGen.class)
        Map<String, String> firstGroupMap;

        @NumberRule(group = GROUP_2)
        Integer secondGroupInt;

        @BooleanRule(group = REQUIRED)
        Boolean reqBoolean;

        @BooleanRule
        Boolean defaultBoolean;
    }

    @Getter
    @NoArgsConstructor
    static class DtoInner {

        @StringRule(group = REQUIRED)
        String reqStr;

        @NumberRule(group = REQUIRED)
        Integer reqInt;

        @StringRule
        String defaultStr;

        @NumberRule
        Integer defaultInt;

        @CustomRule(generatorClass = MapGen.class)
        Map<String, String> defaultMap;

        @NumberRule(group = GROUP_1)
        Integer firstGroupInt;

        @CustomRule(generatorClass = MapGen.class, group = GROUP_1)
        Map<String, String> firstGroupMap;

        @NumberRule(group = GROUP_2)
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

        log.info(UtilsRoot.toJson(dto));

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.getReqStr()),
                () -> assertNotNull(dto.getReqInt()),
                () -> assertNotNull(dto.getReqMap()),
                () -> assertNotNull(dto.getReqInnerDto()),
                () -> assertNotNull(dto.getReqInnerDto().getReqInt()),
                () -> assertNotNull(dto.getReqInnerDto().getReqStr()),
                () -> assertNotNull(dto.getReqBoolean()),
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
                () -> assertNull(dto.getSecondGroupInt()),
                () -> assertNull(dto.getDefaultBoolean())
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
