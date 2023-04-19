package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
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

    static class Dto {

        // this annotation do not mean anything
        @StringRule(group = REQUIRED)
        String reqStr;

        @IntegralRule(group = REQUIRED)
        Integer reqInt;

        @NestedDtoRule(group = REQUIRED)
        DtoInner reqInnerDto;

        @CustomRule(group = REQUIRED, generatorClass = MapGen.class)
        Map<String, String> reqMap;

        @StringRule
        String defaultStr;

        @IntegralRule
        Integer defaultInt;

        @NestedDtoRule
        DtoInner defaultInnerDto;

        @IntegralRule(group = GROUP_1)
        Integer firstGroupInt;

        @NestedDtoRule(group = GROUP_1)
        DtoInner firstGroupInnerDto;

        @CustomRule(group = GROUP_1, generatorClass = MapGen.class)
        Map<String, String> firstGroupMap;

        @IntegralRule(group = GROUP_2)
        Integer secondGroupInt;

        @BooleanRule(group = REQUIRED)
        Boolean reqBoolean;

        @BooleanRule
        Boolean defaultBoolean;
    }

    static class DtoInner {

        @StringRule(group = REQUIRED)
        String reqStr;

        @IntegralRule(group = REQUIRED)
        Integer reqInt;

        @StringRule
        String defaultStr;

        @IntegralRule
        Integer defaultInt;

        @CustomRule(generatorClass = MapGen.class)
        Map<String, String> defaultMap;

        @IntegralRule(group = GROUP_1)
        Integer firstGroupInt;

        @CustomRule(generatorClass = MapGen.class, group = GROUP_1)
        Map<String, String> firstGroupMap;

        @IntegralRule(group = GROUP_2)
        Integer secondGroupInt;
    }

    static class MapGen implements CustomGenerator<Map<String, String>> {
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
                () -> assertNotNull(dto.reqStr),
                () -> assertNotNull(dto.reqInt),
                () -> assertNotNull(dto.reqMap),
                () -> assertNotNull(dto.reqInnerDto),
                () -> assertNotNull(dto.reqInnerDto.reqInt),
                () -> assertNotNull(dto.reqInnerDto.reqStr),
                () -> assertNotNull(dto.reqBoolean),
                () -> assertNull(dto.reqInnerDto.defaultMap),
                () -> assertNull(dto.reqInnerDto.defaultInt),
                () -> assertNull(dto.reqInnerDto.defaultStr),
                () -> assertNull(dto.reqInnerDto.firstGroupMap),
                () -> assertNull(dto.reqInnerDto.firstGroupInt),
                () -> assertNull(dto.reqInnerDto.secondGroupInt),
                () -> assertNull(dto.defaultStr),
                () -> assertNull(dto.defaultInt),
                () -> assertNull(dto.defaultInnerDto),
                () -> assertNull(dto.firstGroupInt),
                () -> assertNull(dto.firstGroupMap),
                () -> assertNull(dto.firstGroupInnerDto),
                () -> assertNull(dto.secondGroupInt),
                () -> assertNull(dto.defaultBoolean)
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
                () -> assertNull(dto.reqStr),
                () -> assertNull(dto.reqInt),
                () -> assertNull(dto.reqInnerDto),
                () -> assertNull(dto.reqMap),
                () -> assertNull(dto.defaultStr),
                () -> assertNull(dto.defaultInt),
                () -> assertNull(dto.defaultInnerDto),
                () -> assertNotNull(dto.firstGroupInt),
                () -> assertNotNull(dto.firstGroupMap),
                () -> assertNotNull(dto.firstGroupInnerDto),
                () -> assertNull(dto.secondGroupInt)
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
                () -> assertNotNull(dto.reqStr),
                () -> assertNotNull(dto.reqInt),
                () -> assertNotNull(dto.reqMap),
                () -> assertNotNull(dto.reqInnerDto),
                () -> assertNotNull(dto.reqInnerDto.reqStr),
                () -> assertNotNull(dto.reqInnerDto.reqInt),
                () -> assertNull(dto.reqInnerDto.defaultStr),
                () -> assertNull(dto.reqInnerDto.defaultInt),
                () -> assertNull(dto.reqInnerDto.defaultMap),
                () -> assertNotNull(dto.reqInnerDto.firstGroupInt),
                () -> assertNotNull(dto.reqInnerDto.firstGroupMap),
                () -> assertNull(dto.reqInnerDto.secondGroupInt),
                () -> assertNull(dto.defaultStr),
                () -> assertNull(dto.defaultInt),
                () -> assertNull(dto.defaultInnerDto),
                () -> assertNotNull(dto.firstGroupInt),
                () -> assertNotNull(dto.firstGroupInnerDto),
                () -> assertNotNull(dto.firstGroupMap),
                () -> assertNull(dto.secondGroupInt)
        );
    }
}
