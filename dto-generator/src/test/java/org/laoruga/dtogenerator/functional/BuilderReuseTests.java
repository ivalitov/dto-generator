package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.functional.data.dto.DtoAllKnownTypes;
import org.laoruga.dtogenerator.rules.RulesInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;

import static org.exparity.hamcrest.date.LocalDateMatchers.sameOrAfter;
import static org.exparity.hamcrest.date.LocalDateMatchers.sameOrBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
@DisplayName("Builder reuse")
@Epic("BUILDER_REUSE")
@Slf4j
class BuilderReuseTests {

    @Test
    @DisplayName("Generating of two DTO instances")
    void generateToDtoInstances() {

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(DtoAllKnownTypes.class);
        builder.getUserConfig().getGenBuildersConfig().getListConfig().setCollectionInstance(LinkedList::new);

        DtoGenerator<DtoAllKnownTypes> dtoGenerator = builder.build();
        DtoAllKnownTypes dto_1 = dtoGenerator.generateDto();
        DtoAllKnownTypes dto_2 = dtoGenerator.generateDto();

        fieldsAssertions(dto_1);
        fieldsAssertions(dto_2);

        assertNotSame(dto_1, dto_2);

        assertAll(
                () -> assertNotEquals(dto_1, dto_2),
                () -> assertNotEquals(dto_1.hashCode(), dto_2.hashCode()),
                () -> assertNotEquals(dto_1.getString(), dto_2.getString()),
                () -> assertNotEquals(dto_1.getALong(), dto_2.getALong())
        );

    }

    @Test
    @DisplayName("Updating one DTO instance")
    void generateTwoVersionOfOneInstance() {

        DtoAllKnownTypes dto = new DtoAllKnownTypes();

        DtoGeneratorBuilder<DtoAllKnownTypes> builder = DtoGenerator.builder(dto);
        builder.getUserConfig().getGenBuildersConfig().getListConfig().setCollectionInstance(LinkedList::new);

        DtoGenerator<DtoAllKnownTypes> dtoGenerator = builder.build();
        DtoAllKnownTypes dto_1 = dtoGenerator.generateDto();
        fieldsAssertions(dto_1);
        String stringFromDto1 = dto_1.getString();
        Long longFromDto1 = dto_1.getALong();

        DtoAllKnownTypes dto_2 = dtoGenerator.generateDto();
        fieldsAssertions(dto_2);
        String stringFromDto2 = dto_2.getString();
        Long longFromDto2 = dto_2.getALong();

        assertAll(
                () -> assertSame(dto, dto_1),
                () -> assertSame(dto_1, dto_2),
                () -> assertNotEquals(stringFromDto1, stringFromDto2),
                () -> assertNotEquals(longFromDto1, longFromDto2)
        );
    }

    private void fieldsAssertions(DtoAllKnownTypes dto) {
        LocalDate now = LocalDateTime.now().toLocalDate();
        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.stringRule.minLength())).and(
                        lessThanOrEqualTo(RulesInstance.stringRule.maxLength()))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.longRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.longRule.maxValue()))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.doubleRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.doubleRule.maxValue()))),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), both(
                        sameOrAfter(now.minusDays(RulesInstance.localDateTimeRule.leftShiftDays()))).and(
                        sameOrBefore(now.plusDays(RulesInstance.localDateTimeRule.rightShiftDays())))),
                () -> assertThat(dto.getClientType(), notNullValue()),

                () -> assertThat(dto.getListOfString().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),
                () -> assertThat(dto.getSetOfLong().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),
                () -> assertThat(dto.getLinkedListOfEnum().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),

                () -> assertThat(dto.getInnerDto(), notNullValue()),
                () -> assertThat(dto.getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getCustomInteger(), equalTo(888))
        );


        assertAll(
                () -> assertThat(dto.getString().length(), both(
                        greaterThanOrEqualTo(RulesInstance.stringRule.minLength())).and(
                        lessThanOrEqualTo(RulesInstance.stringRule.maxLength()))),
                () -> assertThat(dto.getInteger(), both(
                        greaterThanOrEqualTo(RulesInstance.integerRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.integerRule.maxValue()))),
                () -> assertThat(dto.getALong(), both(
                        greaterThanOrEqualTo(RulesInstance.longRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.longRule.maxValue()))),
                () -> assertThat(dto.getADouble(), both(
                        greaterThanOrEqualTo(RulesInstance.doubleRule.minValue())).and(
                        lessThanOrEqualTo(RulesInstance.doubleRule.maxValue()))),
                () -> assertThat(dto.getLocalDateTimeAsIs().toLocalDate(), both(
                        sameOrAfter(now.minusDays(RulesInstance.localDateTimeRule.leftShiftDays()))).and(
                        sameOrBefore(now.plusDays(RulesInstance.localDateTimeRule.rightShiftDays())))),
                () -> assertThat(dto.getClientType(), notNullValue()),

                () -> assertThat(dto.getListOfString().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),
                () -> assertThat(dto.getSetOfLong().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),
                () -> assertThat(dto.getLinkedListOfEnum().size(), both(
                        greaterThanOrEqualTo(RulesInstance.listRule.minSize())).and(
                        lessThanOrEqualTo(RulesInstance.listRule.maxSize()))),

                () -> assertThat(dto.getInnerDto().getStringIntegerMap(), nullValue()),

                () -> assertThat(dto.getInnerDto().getCustomInteger(), equalTo(999))
        );
    }

}
