package org.laoruga.dtogenerator.functional;

import io.qameta.allure.Epic;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.rules.BooleanRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.laoruga.dtogenerator.constants.RuleRemark.*;

/**
 * @author Il'dar Valitov
 * Created on 27.02.2023
 */

@DisplayName("Boolean Rules Tests")
@Epic("BOOLEAN_RULES")
public class BooleanGenerationTests {

    @Getter
    static class Dto {

        @BooleanRule
        Boolean booleanObjectDefaultRule;

        @BooleanRule
        boolean booleanPrimitiveDefaultRule;

        @BooleanRule(trueProbability = 0)
        Boolean booleanObjectAlwaysFalse;

        @BooleanRule(trueProbability = 1)
        boolean booleanPrimitiveAlwaysTrue;

        Boolean booleanObject;

        boolean booleanPrimitive;

        @BooleanRule(trueProbability = 0.8)
        Boolean highProbability;

        @BooleanRule(trueProbability = 0.2)
        boolean lowProbability;
    }

    @RepeatedTest(5)
    @DisplayName("Boolean Generating")
    void booleanGeneratingTests() {
        Dto dto = DtoGenerator.builder(Dto.class).build().generateDto();
        assertAll(
                () -> assertThat(dto.getBooleanObjectDefaultRule(), notNullValue()),
                () -> assertThat(dto.getBooleanObjectAlwaysFalse(), equalTo(false)),
                () -> assertThat(dto.isBooleanPrimitiveAlwaysTrue(), equalTo(true)),
                () -> assertThat(dto.getBooleanObject(), nullValue()),
                () -> assertThat(dto.isBooleanPrimitive(), equalTo(false))
        );
    }

    @RepeatedTest(10)
    @DisplayName("Probability Test")
    void probabilityTest() {
        DtoGenerator<Dto> builder = DtoGenerator.builder(Dto.class).build();

        int attempts = 100;

        // idx 0 - true, idx 1 - false
        int[] highProbabilityResults = new int[2];
        int[] lowProbabilityResults = new int[2];
        int[] randomProbabilityResults = new int[2];

        for (int i = 0; i < attempts; i++) {
            Dto dto = builder.generateDto();

            if (dto.getHighProbability()) {
                highProbabilityResults[0]++;
            } else {
                highProbabilityResults[1]++;
            }

            if (dto.isLowProbability()) {
                lowProbabilityResults[0]++;
            } else {
                lowProbabilityResults[1]++;
            }

            if (dto.getBooleanObjectDefaultRule()) {
                randomProbabilityResults[0]++;
            } else {
                randomProbabilityResults[1]++;
            }
        }

        int approximatedAverageMinimum = (int) ((attempts / 2) - attempts * 0.15);

        assertAll(
                () -> assertThat(highProbabilityResults[0], greaterThan(highProbabilityResults[1])),
                () -> assertThat(lowProbabilityResults[0], lessThan(lowProbabilityResults[1])),
                () -> assertThat(randomProbabilityResults[0], greaterThan(approximatedAverageMinimum)),
                () -> assertThat(randomProbabilityResults[0], greaterThan(approximatedAverageMinimum))
        );
    }

    @RepeatedTest(2)
    @DisplayName("Boolean Rule Remark")
    void booleanRuleRemark() {

        Dto dto = DtoGenerator.builder(Dto.class).setRuleRemark(MIN_VALUE).build().generateDto();

        assertAll(
                () -> assertThat(dto.getBooleanObjectDefaultRule(), notNullValue()),
                () -> assertThat(dto.getBooleanObjectAlwaysFalse(), equalTo(false)),
                () -> assertThat(dto.isBooleanPrimitiveAlwaysTrue(), equalTo(false)),
                () -> assertThat(dto.getBooleanObject(), nullValue()),
                () -> assertThat(dto.isBooleanPrimitive(), equalTo(false))
        );

        DtoGeneratorBuilder<Dto> builder = DtoGenerator.builder(Dto.class);

        builder.setRuleRemark(MAX_VALUE);
        builder.setRuleRemark("booleanObjectDefaultRule", NULL_VALUE);

        Dto dto_2 = builder.build().generateDto();

        assertAll(
                () -> assertThat(dto_2.getBooleanObjectDefaultRule(), nullValue()),
                () -> assertThat(dto_2.getBooleanObjectAlwaysFalse(), equalTo(true)),
                () -> assertThat(dto_2.isBooleanPrimitiveAlwaysTrue(), equalTo(true)),
                () -> assertThat(dto_2.getBooleanObject(), nullValue()),
                () -> assertThat(dto_2.isBooleanPrimitive(), equalTo(false))
        );

    }


}
