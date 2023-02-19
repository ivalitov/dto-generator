package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.LongGeneratorBuilder;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class LongGenerator implements IGenerator<Long> {

    private final long maxValue;
    private final long minValue;
    private final IRuleRemark ruleRemark;

    @Override
    public Long generate() {
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            return minValue + (long) (Math.random() * (maxValue - minValue));
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static LongGeneratorBuilder builder() {
        return new LongGeneratorBuilder();
    }

}
