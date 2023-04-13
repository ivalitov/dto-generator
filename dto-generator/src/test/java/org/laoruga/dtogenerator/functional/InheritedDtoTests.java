package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("Inherited Dto Tests")
@Epic("INHERITED_DTO")
class InheritedDtoTests {

    static class Dto extends DtoSuper {
        @NumberRule()
        Integer upperInt;
        @StringRule()
        String upperString;
    }

    static class DtoSuper extends DtoSuperSuper {

        @CollectionRule(element = @Entry(stringRule = @StringRule))
        List<String> superList;
        @DateTimeRule
        LocalDateTime superDateTime;
    }

    static class DtoSuperSuper {

        @MapRule
        Map<String, Long> superSuperMap;
        @DecimalRule
        Double superSuperDouble;
    }

    @Test
    @DisplayName("Super Class Generation")
    void generationWithInheritanceAsIs() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.upperInt),
                () -> assertNotNull(dto.upperString)
        );
        assertAll(
                () -> assertNotNull(dto.superList),
                () -> assertNotNull(dto.superDateTime)
        );
        assertAll(
                () -> assertNotNull(dto.superSuperMap),
                () -> assertNotNull(dto.superSuperDouble)
        );
    }

    static class Dto_2 extends DtoSuper_2 {
        @NumberRule()
        Integer upperInt;
        @StringRule()
        String upperString;
        @NestedDtoRule
        DtoNested upperDtoNested;
    }

    static class DtoSuper_2 extends DtoSuperSuper_2 {

        @CollectionRule(element = @Entry(stringRule = @StringRule))
        List<String> superList;
        @DateTimeRule
        LocalDateTime superDateTime;
        @NestedDtoRule
        DtoNested superDtoNested;
    }

    static class DtoSuperSuper_2 {

        @MapRule
        Map<String, Long> superSuperMap;
        @DecimalRule
        Double superSuperDouble;
//        @NestedDtoRule
//        DtoNested superDtoNested;
    }

    static class DtoNested {

        @ArrayRule
        String[] nestedArray;
        @BooleanRule
        Boolean nestedBoolean;
    }

    @Disabled
    @Test
    @DisplayName("Generation WithInheritance And Nested DTOs")
    void generationWithInheritanceAndNestedDTOs() {
        Dto_2 dto = DtoGenerator.builder(Dto_2.class).build().generateDto();

        assertNotNull(dto);
        assertAll(
                () -> assertNotNull(dto.upperInt),
                () -> assertNotNull(dto.upperString),
                () -> assertNotNull(dto.upperDtoNested.nestedArray),
                () -> assertNotNull(dto.upperDtoNested.nestedBoolean)
        );
        assertAll(
                () -> assertNotNull(dto.superList),
                () -> assertNotNull(dto.superDateTime),
                () -> assertNotNull(dto.superDtoNested.nestedArray),
                () -> assertNotNull(dto.superDtoNested.nestedBoolean)
        );
        assertAll(
                () -> assertNotNull(dto.superSuperMap),
                () -> assertNotNull(dto.superSuperDouble)
        );
    }

}
