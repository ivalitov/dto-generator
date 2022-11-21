package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.Group;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.constants.BasicRuleRemark.*;
import static org.laoruga.dtogenerator.constants.CharSet.ENG;
import static org.laoruga.dtogenerator.constants.CharSet.NUM;
import static org.laoruga.dtogenerator.constants.Group.*;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("RULES_GROUPING")
class RulesGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @IntegerRule(group = GROUP_1, ruleRemark = MAX_VALUE)
        @IntegerRule(group = GROUP_2, ruleRemark = MIN_VALUE)
        @IntegerRule(ruleRemark = RANDOM_VALUE, minValue = 10, maxValue = 10)
        private Integer intFirst;

        @IntegerRule(group = GROUP_1, ruleRemark = MIN_VALUE)
        @IntegerRule(group = Group.GROUP_3, minValue = 99, maxValue = 99)
        private Integer intSecond;

        @IntegerRule(minValue = 0, maxValue = 0)
        private Integer defaultGroup;
    }

    @Getter
    @NoArgsConstructor
    static class DtoList {

        //GR_1
        @ListRule(group = GROUP_1, minSize = 1, maxSize = 1)
        @IntegerRule(group = GROUP_1, ruleRemark = MAX_VALUE)
        //GR_2
        @ListRule(group = GROUP_2, minSize = 2, maxSize = 2)
        @IntegerRule(group = GROUP_2, ruleRemark = MIN_VALUE)
        //DEFAULT
        @ListRule(minSize = 3, maxSize = 3)
        @IntegerRule(minValue = 10, maxValue = 10)
        private List<Integer> intList;

        @ListRule(minSize = 6, maxSize = 6)
        @IntegerRule
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
                () -> assertThat(dto.getIntFirst(), equalTo(IntegerRule.DEFAULT_MAX)),
                () -> assertThat(dto.getIntSecond(), equalTo(IntegerRule.DEFAULT_MIN)),
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
                () -> assertThat(dto.getIntFirst(), equalTo(IntegerRule.DEFAULT_MIN)),
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
                () -> assertThat(dto.getIntListDefault(), hasSize(6)),
                () -> assertThat(dto.getIntListDefault(), everyItem(both(
                        greaterThanOrEqualTo(IntegerRule.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRule.DEFAULT_MAX)))));
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
                () -> assertThat(dtoDefault.getLocalDateTime().toLocalDate(), equalTo(LocalDate.now().plusDays(1))),
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

        @StringRule(group = GROUP_1, minSymbols = 1, maxSymbols = 1, charset = NUM)
        @StringRule(charset = ENG)
        private String string;

        @SetRule(group = GROUP_1, minSize = 1, maxSize = 1)
        @IntegerRule(group = GROUP_1, minValue = 1, maxValue = 1)
        @SetRule(minSize = 2, maxSize = 2)
        @IntegerRule(minValue = 2, maxValue = 3)
        private Set<Integer> set;

        @LongRule(group = GROUP_1, minValue = 1, maxValue = 1)
        @LongRule(minValue = 2, maxValue = 2)
        private Long aLong;

        @LocalDateTimeRule(group = GROUP_1, leftShiftDays = 0, rightShiftDays = 0)
        @LocalDateTimeRule(leftShiftDays = -1, rightShiftDays = 1)
        private LocalDateTime localDateTime;

        @ListRule(group = GROUP_1, minSize = 1, maxSize = 1)
        @DoubleRule(group = GROUP_1, minValue = 1, maxValue = 1)
        @ListRule(minSize = 2, maxSize = 2)
        @DoubleRule(minValue = 2, maxValue = 2)
        private List<Double> list;

        @IntegerRule(group = GROUP_1, minValue = 1, maxValue = 1)
        @IntegerRule(minValue = 2, maxValue = 2)
        private int integer;

        @DoubleRule(group = GROUP_1, minValue = 1, maxValue = 1)
        @DoubleRule(minValue = 2, maxValue = 2)
        private Double aDouble;

        @EnumRule(group = GROUP_1, enumClass = SomeEnum.class, possibleEnumNames = "FOO")
        @EnumRule(enumClass = SomeEnum.class, possibleEnumNames = "BAR")
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
        public CustomGen setArgs(String[] args) {
            arg = args[0];
            return this;
        }
    }

}
