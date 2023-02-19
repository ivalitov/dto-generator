package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.IntegerGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class IntegerGenerator implements IGenerator<Integer> {

    private final int maxValue;
    private final int minValue;
    private final IRuleRemark ruleRemark;

    @Override
    public Integer generate() {
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return RandomUtils.nextInt(minValue, maxValue);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static IntegerGeneratorBuilder builder() {
        return new IntegerGeneratorBuilder();
    }

}
