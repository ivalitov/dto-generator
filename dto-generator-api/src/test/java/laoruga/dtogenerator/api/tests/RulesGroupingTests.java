package laoruga.dtogenerator.api.tests;

import io.qameta.allure.Epic;
import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.markup.rules.IntegerRule;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.api.markup.rules.StringRules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("RULES_GROUPING")
public class RulesGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @IntegerRule(group = Group.GROUP_1, ruleRemark = MAX_VALUE)
        @IntegerRule(group = Group.GROUP_2, ruleRemark = MIN_VALUE)
        @IntegerRule(ruleRemark = RANDOM_VALUE, minValue = 10, maxValue = 10)
        private Integer intFirst;

        @IntegerRule(group = Group.GROUP_1, ruleRemark = MIN_VALUE)
        @IntegerRule(group = Group.GROUP_3, minValue = 99, maxValue = 99)
        private Integer intSecond;

        @IntegerRule(minValue = 0, maxValue = 0)
        private Integer defaultGroup;

    }

    @DisplayName("Default group")
    @Test
    public void defaultGroup() {
        Dto dto  = DtoGenerator.builder().build().generateDto(Dto.class);
        assertAll(
                () -> assertThat(dto.getIntFirst(), equalTo(10)),
                () -> assertThat(dto.getIntSecond(), nullValue()),
                () -> assertThat(dto.getDefaultGroup(), equalTo(0))
        );

    }

    @DisplayName("Include group")
    @Test
    public void includeGroup() {
        Dto dto  = DtoGenerator.builder()
                .includeGroups(Group.GROUP_1)
                .build()
                .generateDto(Dto.class);
        assertAll(
                () -> assertThat(dto.getIntFirst(), equalTo(IntegerRule.DEFAULT_MAX)),
                () -> assertThat(dto.getIntSecond(), equalTo(IntegerRule.DEFAULT_MIN)),
                () -> assertThat(dto.getDefaultGroup(), nullValue())
        );

    }

}
