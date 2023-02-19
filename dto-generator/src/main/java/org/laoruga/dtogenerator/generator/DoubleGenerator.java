package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Precision;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.DoubleGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DoubleGenerator implements IGenerator<Double> {

    private final double maxValue;
    private final double minValue;
    private final int precision;
    private final IRuleRemark ruleRemark;

    @Override
    public Double generate() {
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return minValue;
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return maxValue;
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            double generated = minValue + RandomUtils.getRandom().nextDouble() * (maxValue - minValue);
            return Precision.round(generated, precision);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static DoubleGeneratorBuilder builder() {
        return new DoubleGeneratorBuilder();
    }

}
