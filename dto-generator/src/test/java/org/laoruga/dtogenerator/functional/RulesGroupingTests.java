package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.Extensions;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RulesInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.constants.CharSet.ENG;
import static org.laoruga.dtogenerator.constants.CharSet.NUM;
import static org.laoruga.dtogenerator.constants.Group.*;
import static org.laoruga.dtogenerator.constants.RuleRemark.*;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("Rule Grouping Tests")
@Epic("RULES_GROUPING")
@ExtendWith(Extensions.RestoreStaticConfig.class)
class RulesGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @NumberRule(group = GROUP_1, ruleRemark = MAX_VALUE)
        @NumberRule(group = GROUP_2, ruleRemark = MIN_VALUE)
        @NumberRule(ruleRemark = RANDOM_VALUE, minInt = 10, maxInt = 10)
        private Integer intFirst;

        @NumberRule(group = GROUP_1, ruleRemark = MIN_VALUE)
        @NumberRule(group = Group.GROUP_3, minInt = 99, maxInt = 99)
        private Integer intSecond;

        @NumberRule(minInt = 0, maxInt = 0)
        private Integer defaultGroup;
    }

    @Getter
    @NoArgsConstructor
    static class DtoList {

        //GR_1
        @CollectionRule(
                group = GROUP_1, minSize = 1, maxSize = 1,
                element = @Entry(numberRule = @NumberRule(ruleRemark = MAX_VALUE)))
        //GR_2
        @CollectionRule(
                group = GROUP_2, minSize = 2, maxSize = 2,
                element = @Entry(numberRule = @NumberRule(ruleRemark = MIN_VALUE)))
        //DEFAULT
        @CollectionRule(
                minSize = 3, maxSize = 3,
                element = @Entry(numberRule = @NumberRule(minInt = 10, maxInt = 10)))
        private List<Integer> intList;

        @CollectionRule(
                minSize = 6, maxSize = 6,
                element = @Entry(numberRule = @NumberRule))
        private List<Integer> intListDefault;
    }

    @DisplayName("Default group")
    @Test
    void defaultGroup() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();
        assertAll(
                () -> assertThat(dto.getIntFirst(), equalTo(10)),
                () -> assertThat(dto.getIntSecond(), nullValue()),
                () -> assertThat(dto.getDefaultGroup(), equalTo(0))
        );
    }

    @DisplayName("Include group")
    @Test
    void includeGroup() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .includeGroups(GROUP_1)
                .build()
                .generateDto();
        assertAll(
                () -> assertThat(dto.getIntFirst(), equalTo(RulesInstance.NUMBER_RULE.maxInt())),
                () -> assertThat(dto.getIntSecond(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getDefaultGroup(), nullValue())
        );
    }

    @DisplayName("Include groups")
    @Test
    void includeGroups() {
        Dto dto = DtoGenerator.builder(Dto.class)
                .includeGroups(GROUP_2, GROUP_3)
                .build()
                .generateDto();
        assertAll(
                () -> assertThat(dto.getIntFirst(), equalTo(RulesInstance.NUMBER_RULE.minInt())),
                () -> assertThat(dto.getIntSecond(), equalTo(99)),
                () -> assertThat(dto.getDefaultGroup(), nullValue())
        );
    }

    @DisplayName("List rules group")
    @Test
    void listRulesDefault() {
        DtoList dto = DtoGenerator.builder(DtoList.class)
                .build()
                .generateDto();
        assertAll(
                () -> assertThat(dto.getIntList(), both(hasSize(3))
                        .and(everyItem(equalTo(10)))),
                () -> assertThat(dto.getIntListDefault(), hasSize(6))
        );
        for (Integer integer : dto.getIntListDefault()) {
            assertThat(integer,
                    both(greaterThanOrEqualTo(RulesInstance.NUMBER_RULE.minInt()))
                            .and(lessThanOrEqualTo(RulesInstance.NUMBER_RULE.maxInt())));
        }
    }

    /*
     * All types test
     */

    @DisplayName("All rules grouping")
    @Test
    void allRulesGrouping() {
        DtoAllRules dtoGroup = DtoGenerator.builder(DtoAllRules.class)
                .includeGroups(GROUP_1)
                .build()
                .generateDto();
        assertAll(
                () -> assertThat(dtoGroup.getString(), matchesRegex("[0-9]")),
                () -> assertThat(dtoGroup.getSet(), both(hasSize(1)).and(contains(1))),
                () -> assertThat(dtoGroup.getALong(), equalTo(1L)),
                () -> assertThat(dtoGroup.getLocalDateTime().toLocalDate(), equalTo(LocalDate.now())),
                () -> assertThat(dtoGroup.getList(), equalTo(Arrays.asList(1D))),
                () -> assertThat(dtoGroup.getInteger(), equalTo(1)),
                () -> assertThat(dtoGroup.getADouble(), equalTo(1D)),
                () -> assertThat(dtoGroup.getAEnum(), equalTo(SomeEnum.FOO)),
                () -> assertThat(dtoGroup.getCustomDto().getArg(), equalTo("1"))
        );

        DtoAllRules dtoDefault = DtoGenerator.builder(DtoAllRules.class)
                .build()
                .generateDto();

        assertAll(
                () -> assertThat(dtoDefault.getString(), matchesRegex("[a-zA-Z]*")),
                () -> assertThat(dtoDefault.getSet(), equalTo(new HashSet<>(Arrays.asList(2, 3)))),
                () -> assertThat(dtoDefault.getALong(), equalTo(2L)),
                () -> assertThat(dtoDefault.getLocalDateTime().toLocalDate(), equalTo(LocalDate.now().minusDays(1))),
                () -> assertThat(dtoDefault.getList(), equalTo(Arrays.asList(2D, 2D))),
                () -> assertThat(dtoDefault.getInteger(), equalTo(2)),
                () -> assertThat(dtoDefault.getADouble(), equalTo(2D)),
                () -> assertThat(dtoDefault.getAEnum(), equalTo(SomeEnum.BAR)),
                () -> assertThat(dtoDefault.getCustomDto().getArg(), equalTo("2"))
        );
    }

    @Getter
    @NoArgsConstructor
    static class DtoAllRules {

        @StringRule(group = GROUP_1, minLength = 1, maxLength = 1, chars = NUM)
        @StringRule(chars = ENG)
        private String string;

        @CollectionRule(
                group = GROUP_1, minSize = 1, maxSize = 1,
                element = @Entry(numberRule = @NumberRule(minInt = 1, maxInt = 1)))

        @CollectionRule(
                minSize = 2, maxSize = 2,
                element = @Entry(numberRule = @NumberRule(minInt = 2, maxInt = 3)))
        private Set<Integer> set;

        @NumberRule(group = GROUP_1, minLong = 1, maxLong = 1)
        @NumberRule(minLong = 2, maxLong = 2)
        private Long aLong;

        @DateTimeRule(group = GROUP_1)
        @DateTimeRule(chronoUnitShift = @ChronoUnitShift(unit = ChronoUnit.DAYS, leftBound = -1, rightBound = -1))
        private LocalDateTime localDateTime;

        @CollectionRule(
                group = GROUP_1, minSize = 1, maxSize = 1,
                element = @Entry(decimalRule = @DecimalRule(group = GROUP_1, minDouble = 1, maxDouble = 1)))

        @CollectionRule(
                minSize = 2, maxSize = 2,
                element = @Entry(decimalRule = @DecimalRule(minDouble = 2, maxDouble = 2)))
        private List<Double> list;

        @NumberRule(group = GROUP_1, minInt = 1, maxInt = 1)
        @NumberRule(minInt = 2, maxInt = 2)
        private int integer;

        @DecimalRule(group = GROUP_1, minDouble = 1, maxDouble = 1)
        @DecimalRule(minDouble = 2, maxDouble = 2)
        private Double aDouble;

        @EnumRule(group = GROUP_1, possibleEnumNames = "FOO")
        @EnumRule(possibleEnumNames = "BAR")
        private SomeEnum aEnum;

        @CustomRule(group = GROUP_1, generatorClass = CustomGen.class, args = "1")
        @CustomRule(generatorClass = CustomGen.class, args = "2")
        private CustomDto customDto;

    }

    enum SomeEnum {
        FOO,
        BAR
    }

    @Value
    static class CustomDto {
        String arg;
    }

    static class CustomGen implements ICustomGeneratorArgs<CustomDto> {

        String arg;

        @Override
        public CustomDto generate() {
            return new CustomDto(arg);
        }

        @Override
        public void setArgs(String[] args) {
            arg = args[0];
        }
    }

    static class ArraysDto {

        @ArrayRule(minSize = 1, maxSize = 1,
                element = @Entry(stringRule = @StringRule))
        @ArrayRule(group = GROUP_1,
                minSize = 2, maxSize = 2,
                element = @Entry(stringRule = @StringRule))
        String[] strings;

        @ArrayRule(minSize = 1, maxSize = 1,
                element = @Entry(numberRule = @NumberRule))
        @ArrayRule(group = GROUP_2,
                minSize = 2, maxSize = 2,
                element = @Entry(numberRule = @NumberRule))
        Integer[] integers;

        @ArrayRule(minSize = 1, maxSize = 1,
                element = @Entry(numberRule = @NumberRule))
        @ArrayRule(group = GROUP_2,
                minSize = 2, maxSize = 2,
                element = @Entry(numberRule = @NumberRule))
        @ArrayRule(group = GROUP_3,
                minSize = 3, maxSize = 3,
                element = @Entry(numberRule = @NumberRule))
        long[] longs;

    }

    @Test
    @DisplayName("Array rules grouping")
    void arraysGrouping() {

        DtoGeneratorBuilder<ArraysDto> builder = DtoGenerator.builder(ArraysDto.class);

        ArraysDto dtoDefault;

        // DEFAULT group
        dtoDefault = builder.build().generateDto();
        assertAll(
                () -> assertThat(dtoDefault.strings.length, equalTo(1)),
                () -> assertThat(dtoDefault.integers.length, equalTo(1)),
                () -> assertThat(dtoDefault.longs.length, equalTo(1))
        );

        // GROUP_2
        ArraysDto dtoGroup2 = builder.includeGroups(GROUP_2).build().generateDto();

        assertAll(
                () -> assertThat(dtoGroup2.strings, nullValue()),
                () -> assertThat(dtoGroup2.integers.length, equalTo(2)),
                () -> assertThat(dtoGroup2.longs.length, equalTo(2))
        );

        // GROUP_3
        ArraysDto dtoGroup2and3 = DtoGenerator.builder(ArraysDto.class)
                .includeGroups(GROUP_3).build().generateDto();

        assertAll(
                () -> assertThat(dtoGroup2and3.strings, nullValue()),
                () -> assertThat(dtoGroup2and3.integers, nullValue()),
                () -> assertThat(dtoGroup2and3.longs.length, equalTo(3))
        );
    }

}
