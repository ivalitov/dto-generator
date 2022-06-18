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

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.MAX_VALUE;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.MIN_VALUE;

/**
 * @author Il'dar Valitov
 * Created on 02.06.2022
 */
@DisplayName("String Rules Tests")
@Epic("STRING_RULES")
public class RulesGroupingTests {

    @Getter
    @NoArgsConstructor
    static class Dto {

        @IntegerRule(group = Group.GROUP_1, ruleRemark = MAX_VALUE)
        @IntegerRule(group = Group.GROUP_2, ruleRemark = MIN_VALUE)
        private Integer intFirst;

    }


    @RepeatedTest(1)
    @DisplayName("Generated string by mask (phone number)")
    public void maskPhoneNumber() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
    }

}
